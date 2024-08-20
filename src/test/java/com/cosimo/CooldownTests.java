package com.cosimo;

import com.cosimo.utilities.timed.Cooldown;
import com.cosimo.utilities.timed.Cooldowns;
import com.cosimo.utilities.timed.ICooldown;
import com.cosimo.utilities.timed.TimeStandard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class CooldownTests {

    /**
     * Permitted time difference from setting a time value to testing it.
     */
    public static final int TOLERANCE = 50;

    @Test
    public void testToThisTime() {
        final TimeStandard timeStandard = new TimeStandard() {
        };
        assertEquals(60_000, timeStandard.toThisTime(1, TimeUnit.MINUTES));
        assertEquals(1_000, timeStandard.toThisTime(1, TimeUnit.SECONDS));
    }

    @Test
    public void testFromThisTime() {
        final TimeStandard timeStandard = new TimeStandard() {
        };
        assertEquals(1, timeStandard.fromThisTime(60_000, TimeUnit.MINUTES));
        assertEquals(1, timeStandard.fromThisTime(1_000, TimeUnit.SECONDS));
    }

    @Test
    public void testGetCurrentTime() {
        final TimeStandard timeStandard = new TimeStandard() {
        };
        final long currentTime = System.currentTimeMillis();

        assertTrue(Math.abs(timeStandard.getCurrentTime() - currentTime) < TOLERANCE);
    }

    @Test
    public void testIsExpired() throws InterruptedException {
        final ICooldown cooldown = new Cooldown(50);
        assertFalse(cooldown.isExpired());
        Thread.sleep(60);
        assertTrue(cooldown.isExpired());
    }

    @Test
    public void testGetRemaining() {
        final ICooldown cooldown = new Cooldown(5_000);
        final long remaining = cooldown.getRemaining();

        assertTrue(remaining <= 5_000 && remaining > 0);
        assertTrue(cooldown.getRemaining(TimeUnit.SECONDS) <= 5);
    }

    @Test
    public void testCooldownConstructorWithTimeUnit() {
        final Cooldown cooldown = new Cooldown(1, TimeUnit.MINUTES);
        assertTrue(cooldown.getExpiration() > System.currentTimeMillis());
    }

    @Test
    public void testCooldownConstructorWithDirectDuration() {
        final Cooldown cooldown = new Cooldown(60_000);
        assertTrue(cooldown.getExpiration() > System.currentTimeMillis());
    }

    @Test
    public void testCooldownExtendWithTimeUnit() {
        final Cooldown cooldown = new Cooldown(5_000);
        final long previousExpiration = cooldown.getExpiration();

        cooldown.extend(10, TimeUnit.SECONDS);
        assertTrue(cooldown.getExpiration() > previousExpiration);
    }

    @Test
    public void testCooldownExtendWithDirectDuration() {
        final Cooldown cooldown = new Cooldown(5_000);
        final long previousExpiration = cooldown.getExpiration();

        cooldown.extend(3_000);
        assertTrue(cooldown.getExpiration() > previousExpiration);
    }

    @Test
    public void testCooldownsCleanup() throws InterruptedException {
        final var cooldowns = new Cooldowns<>();

        cooldowns.put("test1", new Cooldown(50));
        cooldowns.put("test2", new Cooldown(5_000));

        Thread.sleep(50 + TOLERANCE);

        cooldowns.cleanup();

        assertFalse(cooldowns.containsKey("test1"));
        assertTrue(cooldowns.containsKey("test2"));
    }

    @Test
    public void testCooldownsConstructorWithMap() {
        final var initialMap = Map.of("key1", new Cooldown(5000));
        final Cooldowns<String, ICooldown> cooldowns = new Cooldowns<>(initialMap);

        assertTrue(cooldowns.containsKey("key1"));
    }
}