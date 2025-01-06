package com.cosimo.utilities.timed;

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
    public static final int MILLISECOND_TOLERANCE = 50;

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
            @Override
            public long getCurrentTime() {
                return System.nanoTime();
            }
        };
        final long currentTime = System.nanoTime();
        final long diffInMs = Math.abs(timeStandard.getCurrentTime() - currentTime) / 1000;

        assertTrue(diffInMs < MILLISECOND_TOLERANCE);
    }

    @Test
    public void testIsExpired() throws InterruptedException {
        final ICooldown cooldown = new Cooldown(50);

        assertFalse(cooldown.isExpired());

        Thread.sleep(60);

        assertTrue(cooldown.isExpired());

        assertEquals(0, cooldown.getRemaining());
        assertEquals(0, cooldown.getRemaining(TimeUnit.SECONDS));

        assertTrue(cooldown.getDifference() < 0);
        assertTrue(cooldown.getDifference(TimeUnit.NANOSECONDS) < 0);
    }

    @Test
    public void testGetRemainingAndDifference() {
        final ICooldown cooldown = new Cooldown(5_000);
        final long difference = cooldown.getDifference();
        final long remaining = cooldown.getRemaining();

        assertTrue(difference <= 5_000 && difference > 0);
        assertEquals(difference, remaining);
        assertTrue(cooldown.getDifference(TimeUnit.SECONDS) <= 5);
        assertTrue(cooldown.getRemaining(TimeUnit.SECONDS) <= 5);
    }

    @Test
    public void testCooldownConstructorWithTimeUnit() {
        final Cooldown cooldown = new Cooldown(1, TimeUnit.MINUTES);
        assertTrue(cooldown.getExpiration() > System.currentTimeMillis());
    }

    @Test
    public void testCooldownExpirationAfterConstructor() {
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
    public void testCooldownExtendAfterConstructor() {
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

        Thread.sleep(50 + MILLISECOND_TOLERANCE);

        assertTrue(cooldowns.clearExpired());

        assertFalse(cooldowns.containsKey("test1"));
        assertTrue(cooldowns.containsKey("test2"));

        assertFalse(cooldowns.clearExpired());
    }

    @Test
    public void testCooldownsConstructorWithMap() {
        final var initialMap = Map.of("key1", new Cooldown(5000));
        final var cooldowns = new Cooldowns<>(initialMap);

        assertTrue(cooldowns.containsKey("key1"));
    }
}