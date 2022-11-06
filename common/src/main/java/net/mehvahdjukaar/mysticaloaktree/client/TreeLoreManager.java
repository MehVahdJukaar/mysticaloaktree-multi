package net.mehvahdjukaar.mysticaloaktree.client;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight3.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TreeLoreManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final TreeLoreManager INSTANCE = new TreeLoreManager();

    private static final Map<ITreeDialogue.Type<?>, List<ITreeDialogue>> DIALOGUES = new HashMap<>();
    private static final int MAX_SENTENCE_LEN = 80;

    public TreeLoreManager() {
        super(GSON, "tree_wisdom");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        DIALOGUES.clear();
        List<ITreeDialogue> list = new ArrayList<>();
        for (var e : object.entrySet()) {
            JsonElement json = e.getValue();
            if (e.getKey().getPath().equals("countries")) {
                ALL_COUNTRIES.clear();
                var arr = json.getAsJsonObject().get("countries").getAsJsonArray();
                arr.forEach(s -> ALL_COUNTRIES.add(s.getAsString()));
                continue;
            }
            //hack
            var modLoaded = json.getAsJsonObject().get("mod_loaded");

            if (modLoaded == null || PlatformHelper.isModLoaded(modLoaded.getAsString())) {

                var result = ITreeDialogue.CODEC.parse(JsonOps.INSTANCE, json);
                var o = result.resultOrPartial(error -> MysticalOakTree.LOGGER.error("Failed to read tree dialogue JSON object for {} : {}", e.getKey(), error));

                o.ifPresent(list::add);
            }
        }
        for (var l : list) {
            DIALOGUES.computeIfAbsent(l.getType(), o -> new ArrayList<>()).add(l);
        }
        DIALOGUES.values().forEach(Collections::sort);
    }


    @Nullable
    public static ITreeDialogue getRandomDialogue(ITreeDialogue.Type<?> source, Random random, int trust) {
        initIfNeeded();
        if (source == TreeDialogueTypes.TALKED_TO) {
            if (random.nextFloat() < 0.04 && trust >= 40 && TOMORROW_WEATHER != null) {
                return TOMORROW_WEATHER;
            }
            if (random.nextFloat() < 0.05 && trust >= 100 && !RANDOM_FACTS.isEmpty()) {
                return getRandomFact(random);
            }
            if (random.nextFloat() < 0.07 && trust >= 75) {
                return RANDOM_WISDOM_QUOTES.get(random.nextInt(RANDOM_WISDOM_QUOTES.size()));
            }
        }
        var dialogues = DIALOGUES.get(source);
        if (dialogues != null) {

            int upperBound = BinarySearch.find(dialogues, new ITreeDialogue.Dummy(trust)) + 1;
            int delta = trust - source.trustDelta();
            //hack
            int lowerBound = delta <= 0 ? 0 : BinarySearch.find(dialogues, new ITreeDialogue.Dummy(delta));
            if (upperBound > lowerBound) {
                int i = random.nextInt(lowerBound, upperBound+1);
                return dialogues.get(i);
            }
        }
        return null;
    }


    private static final List<ITreeDialogue> RANDOM_WISDOM_QUOTES = Collections.synchronizedList(new ArrayList<>());
    private static final List<ITreeDialogue> RANDOM_FACTS = Collections.synchronizedList(new ArrayList<>());
    private static final List<String> PET_NAMES = Collections.synchronizedList(new ArrayList<>(List.of("blorgle", "splorgle", "garvin", "pepa", "boris")));
    private static final List<String> ALL_COUNTRIES = new ArrayList<>();
    private static ITreeDialogue TOMORROW_WEATHER = null;
    private static String IP = "***";
    private static double LAT = 0;
    private static double LON = 0; //arfican gulf yay
    private static boolean HAS_BEEN_INIT = false;

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    public static void init() {
        initIfNeeded();
    }

    //we don't call on setup so to limit unnecessary API calls
    private static void initIfNeeded() {
        if (!HAS_BEEN_INIT) {
            HAS_BEEN_INIT = true;

            EXECUTORS.submit(TreeLoreManager::addWordsOfWisdom);
            EXECUTORS.submit(TreeLoreManager::addFacts);
            EXECUTORS.submit(TreeLoreManager::addPetNames);
            EXECUTORS.submit(TreeLoreManager::addIP);
        }
    }


    private static void addWordsOfWisdom() {
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Pattern pattern2 = Pattern.compile("\"(.+?)\"");
        String response = readFromURL("https://www.fantasynamegenerators.com/scripts/wisdomQuotes.js");
        Matcher m = pattern.matcher(response);
        while (m.find()) {
            Matcher m2 = pattern2.matcher(m.group(1));
            List<String> l = new ArrayList<>();
            while (m2.find()) {
                String t = m2.group(1).replaceAll("\\(.*?\\)", "");
                if (t.length() > MAX_SENTENCE_LEN) {
                    l = null;
                    break;
                }
                l.add(t);
            }
            if (l != null) RANDOM_WISDOM_QUOTES.add(new ITreeDialogue.Simple(l));
        }
    }

    private static final String API_NINJA_KEY = "Go6dGXfyi+63T+dZcWWV3Q==QbzSr5qgjyBR4ivM";

    private static void addIP() {
        try {
            IP = readFromURL("http://checkip.amazonaws.com/").trim();

            EXECUTORS.submit(TreeLoreManager::addLocation);
        } catch (Exception e) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);

                IP = socket.getLocalAddress().getHostAddress();
            } catch (SocketException | UnknownHostException ignored) {
            }
        }
    }

    private static void addLocation() {
        String page = readFromURL("https://api.api-ninjas.com/v1/iplookup?address=" + IP, true);
        var json = JsonParser.parseString(page).getAsJsonObject();
        LON = json.get("lon").getAsDouble();
        LAT = json.get("lat").getAsDouble();
        EXECUTORS.submit(TreeLoreManager::addWeatherReport);
    }

    private static void addWeatherReport() {
        String page = readFromURL(" http://api.weatherunlocked.com/api/forecast/"
                + LAT + "," + LON + "?app_id=9fb6970a&app_key=f02776b40a5c83cafbb7d072263252a5");
        var json = JsonParser.parseString(page).getAsJsonObject().get("Days").getAsJsonArray();
        var tomorrow = json.get(1).getAsJsonObject().get("Timeframes").getAsJsonArray();
        Multiset<String> desc = HashMultiset.create();
        for (var v : tomorrow) {
            desc.add(v.getAsJsonObject().get("wx_desc").getAsString());
        }
        String weather = desc.stream().max(Comparator.comparingInt(desc::count)).get();
        TOMORROW_WEATHER = new ITreeDialogue.Simple(List.of("I can feel it in the air, tomorrow will be " + weather));
    }

    private static void addFacts() {
        String page = readFromURL("https://api.api-ninjas.com/v1/facts?limit=20", true);
        var json = JsonParser.parseString(page);
        var array = json.getAsJsonArray();
        for (var a : array) {
            String fact = a.getAsJsonObject().get("fact").getAsString();
            var processed = splitSentenceToLen(fact, "");
            if (!processed.isEmpty()) {
                RANDOM_FACTS.add(new ITreeDialogue.Simple(processed));
            }
        }
    }

    private static void addPetNames() {
        String page = readFromURL("https://api.api-ninjas.com/v1/babynames?limit=10", true);
        var json = JsonParser.parseString(page);
        var array = json.getAsJsonArray();
        for (var a : array) {
            String name = a.getAsString();
            PET_NAMES.add(name);
        }
    }

    private static String readFromURL(String link) {
        return readFromURL(link, false);
    }

    private static String readFromURL(String link, boolean apiNinja) {
        StringBuilder content = new StringBuilder();
        try {

            URL url = new URL(link);

            URLConnection connection = url.openConnection();
            if (apiNinja) {
                connection.setRequestProperty("accept", "application/json");
                connection.addRequestProperty("X-Api-Key", API_NINJA_KEY);
            }
            String encoding = connection.getContentEncoding();
            Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line + "\n");
                }
            }
        } catch (Exception ignored) {
        }
        return content.toString();
    }


    /*
    public static String getHTMLText(String url) {
        //Creating a HttpClient object
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            //Creating a HttpGet object
            HttpGet httpget = new HttpGet(url);
            //Executing the Get request
            HttpResponse httpresponse = httpclient.execute(httpget);
            return EntityUtils.toString(httpresponse.getEntity());
        } catch (Exception ignored) {
        }
        return "";
    }

    public static String postHTMLText(String url) {
        //Creating a HttpClient object
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            //Creating a HttpGet object
            HttpPost httpget = new HttpPost(url);
            //Executing the Get request
            HttpResponse httpresponse = httpclient.execute(httpget);
            return EntityUtils.toString(httpresponse.getEntity());
        } catch (Exception ignored) {
        }
        return "";
    }*/


    @Nullable
    private static ITreeDialogue getRandomFact(Random Random) {
        if (RANDOM_FACTS.isEmpty()) return null;
        var d = RANDOM_FACTS.remove(Random.nextInt(RANDOM_FACTS.size()));
        if (RANDOM_FACTS.isEmpty()) {
            EXECUTORS.submit(TreeLoreManager::addFacts);
        }
        return d;
    }


    //keywords
    private static final String IP_KEY = "$ip";
    private static final String PLAYER_NAME_KEY = "$player_name";
    private static final String RANDOM_POS_KEY = "$random_pos";
    private static final String RANDOM_DATE_KEY = "$random_date";
    private static final String RANDOM_NAME_KEY = "$random_name";
    private static final String RANDOM_COUNTRY_KEY = "$random_country";
    private static final int RANDOM_POS_DISTANCE = 1000;

    @NotNull
    public static MutableComponent formatText(String text, @Nonnull Player player) {
        if (text.contains("$")) {
            if (text.contains(IP_KEY)) text = text.replace(IP_KEY, IP);
            if (text.contains(PLAYER_NAME_KEY))
                text = text.replace(PLAYER_NAME_KEY, player.getDisplayName().getString());
            if (text.contains(RANDOM_POS_KEY))
                text = text.replace(RANDOM_POS_KEY,
                        generateRandomPos(player.blockPosition(), player.level.random));
            if (text.contains(RANDOM_DATE_KEY))
                text = text.replace(RANDOM_DATE_KEY, generateRandomDate(player.level.random));
            if (text.contains(RANDOM_NAME_KEY))
                text = text.replace(RANDOM_NAME_KEY, getRandomName(player.level.random));
            if (text.contains(RANDOM_COUNTRY_KEY))
                text = text.replace(RANDOM_COUNTRY_KEY, getRandomCountry(player.level.random));
        }

        return new TranslatableComponent(text);
    }

    private static String getRandomCountry(Random random) {
        return ALL_COUNTRIES.get(random.nextInt(ALL_COUNTRIES.size()));
    }

    private static String getRandomName(Random random) {
        String[] l = new String[]{"Blorg"};

        return l[random.nextInt(l.length)];
    }

    private static String generateRandomDate(Random Random) {
        Calendar c = Calendar.getInstance();
        long startMillis = c.getTime().getTime();
        c.set(4000, Calendar.JANUARY, 0);
        long endMillis = c.getTime().getTime();
        Random random = new Random(Random.nextInt());
        long dateMillis = random.nextLong(startMillis, endMillis);

        var date = new Date(dateMillis);

        c.setTime(date);

        Locale locale = Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected().getCode().replace("_", "-"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyy", locale);
        return dateFormat.format(date);
    }

    private static String generateRandomPos(BlockPos pos, Random Random) {
        var newPos = pos.offset(Random.nextInt(-RANDOM_POS_DISTANCE, RANDOM_POS_DISTANCE+1),
                Random.nextInt(-64, 64),
                Random.nextInt(-RANDOM_POS_DISTANCE, RANDOM_POS_DISTANCE+1));
        return "X = " + newPos.getX() + ", Y = " + newPos.getY() + ", Z = " + newPos.getZ();
    }

    private static final String[] SEPARATORS = new String[]{"\n", "...", ".", "?", "!", ";", ","};

    private static List<String> splitSentenceToLen(String text) {
        return splitSentenceToLen(text, "");
    }

    private static List<String> splitSentenceToLen(String text, String start) {
        List<String> list = new ArrayList<>();
        int len = MAX_SENTENCE_LEN - start.length();
        while (text.length() > len) {
            String max = text.substring(0, len);
            //finds split point
            boolean split = false;
            for (String sep : SEPARATORS) {
                int index = max.lastIndexOf(sep);
                if (index != -1) {
                    var cut = text.substring(0, index);
                    text = text.substring(index);
                    if (list.isEmpty()) cut = start + cut;
                    list.add(cut);
                    split = true;
                    break;
                }
            }
            if (!split) return null;
            //no separator and still too long, aborting
        }
        return list;
    }
}
