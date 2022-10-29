package net.mehvahdjukaar.mysticaloaktree.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class WindParticle extends TextureSheetParticle {
    private final double rollSpeed;
    private boolean hasHitGround;

    WindParticle(ClientLevel clientLevel, double x, double y, double z,
                 double dx, double dy, double dz) {
        super(clientLevel, x, y, z);
        this.friction = 0.998F;

        this.quadSize *= 0.8F + (Math.random() * 2.0 - 1.0) * 0.4F;
        this.lifetime = (int) (14.0 / (this.random.nextFloat() * 0.8 + 0.2));
        this.hasHitGround = false;
        this.hasPhysics = true;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.xd += (Math.random() * 2.0 - 1.0) * 0.03F;
        this.yd += (Math.random() * 2.0 - 1.0) * 0.03F;
        this.zd += (Math.random() * 2.0 - 1.0) * 0.03F;

        this.rollSpeed = 0.35 + Math.random() * 0.2;
        if (Math.random() < 0.5) {
           // var c = new HSLColor(1f, 0*(float) (0.1f * Math.random() + 0.1f), 1f, 1f).asRGB();
           // this.rCol = c.red();
          //  this.gCol = c.green();
          //  this.bCol = c.blue();
        } else {
           // float a =  (float) (0.8+Math.random()*0.2);
          //  //var c = new RGBColor(a,a,1,1);
           //  this.rCol =a;
          //  this.gCol = a;
        }

    }

    @Override
    public void tick() {

        this.oRoll = this.roll;
        this.roll += rollSpeed;


        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {

            boolean wasOnGround = this.hasHitGround;

            if (this.onGround) {
                this.yd = 0.0;
                this.hasHitGround = true;
            }
            if (hasHitGround != wasOnGround) {
                this.xd += (random.nextFloat() * 0.04f);
                this.zd += (random.nextFloat() * 0.04f);
                this.yd += 0.01;
            }

            if (this.hasHitGround) {
                this.yd += 0.002;
            } else {
                this.yd += 0.001;
            }

            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= this.friction;
            this.zd *= this.friction;
            this.yd *= this.friction;
            this.xd += (this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1));
            this.zd += (this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1));
            if (this.age >= this.lifetime / 3f && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.quadSize * Mth.clamp(0.3f + (this.age + partialTicks) / this.lifetime * 4, 0.0F, 1.0F);
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var p = new WindParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            p.pickSprite(this.sprites);
            return p;
        }
    }
}
