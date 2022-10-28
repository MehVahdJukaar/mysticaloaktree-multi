package net.mehvahdjukaar.mysticaloaktree.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TreeLoreManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final TreeLoreManager INSTANCE = new TreeLoreManager();

    private static final Map<ITreeDialogue.Type, List<ITreeDialogue>> DIALOGUES = new HashMap<>();

    public TreeLoreManager() {
        super(GSON, "tree_wisdom");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        DIALOGUES.clear();
        List<ITreeDialogue> list = new ArrayList<>();
        for (var e : object.entrySet()) {
            JsonElement json = e.getValue();
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
    public static ITreeDialogue getRandomDialogue(ITreeDialogue.Type source, RandomSource random, int trust) {
        if (source == TreeDialogueTypes.TALKED_TO && random.nextFloat() < 0.1 && trust >= 75) {
            return RANDOM_WISDOM_QUOTES.get(random.nextInt(RANDOM_WISDOM_QUOTES.size()));
        }
        List<ITreeDialogue> dialogues = DIALOGUES.get(source);
        if (dialogues != null) {

            int upperBound = BinarySearch.find(dialogues, new ITreeDialogue.Dummy(trust)) + 1;
            int delta = trust - source.trustDelta();
            //hack
            int lowerBound = delta <= 0 ? 0 : BinarySearch.find(dialogues, new ITreeDialogue.Dummy(delta));
            if (upperBound > lowerBound) {
                int i = random.nextIntBetweenInclusive(lowerBound, upperBound);
                return dialogues.get(i);
            }
        }

        return null;
    }


    public static final List<ITreeDialogue> RANDOM_WISDOM_QUOTES = Collections.synchronizedList(new ArrayList<>());
    public static final List<String> PET_NAMES = Collections.synchronizedList(new ArrayList<>());
    private static String IP;

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    public static void grabStuffFromTheWEB() {
        EXECUTORS.submit(TreeLoreManager::addWordsOfWisdom);
        EXECUTORS.submit(TreeLoreManager::addPetNames);
        EXECUTORS.submit(TreeLoreManager::addIP);
    }

    private static void addIP() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            IP = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException ignored) {
        }
    }


    private static void addWordsOfWisdom() {
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Pattern pattern2 = Pattern.compile("\"(.+?)\"");
        String page = getHTMLText("https://www.fantasynamegenerators.com/scripts/wisdomQuotes.js");
        Matcher m = pattern.matcher(page);
        while (m.find()) {
            Matcher m2 = pattern2.matcher(m.group(1));
            List<String> l = new ArrayList<>();
            while (m2.find()) {
                String t = m2.group(1).replaceAll("\\(.*?\\)", "");
                if (t.length() > 75) {
                    l = null;
                    break;
                }
                l.add(t);
            }
            if (l != null) RANDOM_WISDOM_QUOTES.add(new ITreeDialogue.Simple(l));
        }
    }

    private static void addPetNames() {
        String page = postHTMLText("https://randommer.io/pet-names");
        int a = 1;
    }


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
    }


    //keywords
    private static final String IP_KEY = "$ip";
    private static final String PLAYER_NAME_KEY = "$player_name";
    private static final String RANDOM_POS_KEY = "$random_pos";
    private static final String RANDOM_DATE_KEY = "$random_date";
    private static final String RANDOM_NAME_KEY = "$random_name";
    private static final int RANDOM_POS_DISTANCE = 1000;

    @NotNull
    public static MutableComponent formatText(String text, @Nonnull Player player) {
        if (text.contains("$")) {
            if (text.contains(IP_KEY)) text = text.replace(IP_KEY, IP);
            if (text.contains(PLAYER_NAME_KEY))
                text = text.replace(PLAYER_NAME_KEY, player.getDisplayName().getString());
            if (text.contains(RANDOM_POS_KEY))
                text = text.replace(RANDOM_POS_KEY,
                        generateRandomPos(player.blockPosition(), player.level.random).toString());
            if (text.contains(RANDOM_DATE_KEY))
                text = text.replace(RANDOM_DATE_KEY, generateRandomDate(player.level.random));
            if (text.contains(RANDOM_NAME_KEY))
                text = text.replace(RANDOM_NAME_KEY, getRandomName(player.level.random));
        }

        return Component.translatable(text);
    }

    private static String getRandomName(RandomSource random) {
        //      List<String> names = List.of("")
        return "mittens";
    }

    private static String generateRandomDate(RandomSource randomSource) {
        Calendar c = Calendar.getInstance();
        long startMillis = c.getTime().getTime();
        c.set(4000, Calendar.JANUARY, 0);
        long endMillis = c.getTime().getTime();
        long dateMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);

        var date = new Date(dateMillis);

        c.setTime(date);

        Locale locale = Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected().getCode().replace("_", "-"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyy", locale);
        return dateFormat.format(date);
    }

    private static String generateRandomPos(BlockPos pos, RandomSource randomSource) {
        var newPos = pos.offset(randomSource.nextIntBetweenInclusive(-RANDOM_POS_DISTANCE, RANDOM_POS_DISTANCE),
                randomSource.nextIntBetweenInclusive(-64, 64),
                randomSource.nextIntBetweenInclusive(-RANDOM_POS_DISTANCE, RANDOM_POS_DISTANCE));
        return "X = " + newPos.getX() + ", Y = " + newPos.getY() + ", Z = " + newPos.getZ();
    }
}
