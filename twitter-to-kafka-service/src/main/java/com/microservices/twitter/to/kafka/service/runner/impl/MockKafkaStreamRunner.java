package com.microservices.twitter.to.kafka.service.runner.impl;

import com.microservices.config.TwitterToKafkaServiceConfigData;
import com.microservices.twitter.to.kafka.service.exception.TwitterToKafkaServiceException;
import com.microservices.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "true")
public class MockKafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private static final Random RANDOM = new Random();
    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
    private static final String TWEET_AS_RAW_JSON = "{" +
            "\"created_at\":\"{0}\"," +
            "\"id\":\"{1}\"," +
            "\"text\":\"{2}\"," +
            "\"user\":{\"id\":\"{3}\"}" +
            "}";
    private static final String[] WORDS = new String[]{
            "Breaking",
            "news",
            "trending",
            "update",
            "funny",
            "amazing",
            "sports",
            "politics",
            "technology",
            "health",
            "science",
            "movie",
            "music",
            "travel",
            "beautiful",
            "awesome",
            "weather",
            "opinion",
            "happy",
            "sad",
            "love",
            "hate",
            "incredible",
            "viral",
            "win",
            "lose",
            "challenge",
            "success",
            "fail",
            "support",
            "community",
            "event",
            "festival",
            "concert",
            "announcement",
            "launch",
            "product",
            "sale",
            "offer",
            "deal",
            "alert",
            "warning",
            "fun",
            "friends",
            "family",
            "work",
            "life",
            "quote",
            "inspiration",
            "motivation",
            "random",
            "story",
            "joke",
            "question",
            "poll",
            "survey",
            "feedback",
            "comment",
            "share",
            "like",
            "follow",
            "retweet",
            "hashtag",
            "trend",
            "influence",
            "blog",
            "video",
            "photo",
            "meme",
            "gossip",
            "breaking",
            "scandal",
            "rumor",
            "startup",
            "innovation",
            "game",
            "challenge",
            "goal",
            "team",
            "strategy",
            "healthcare",
            "finance",
            "economy",
            "business",
            "startup",
            "job",
            "career",
            "learning",
            "coding",
            "developer",
            "data",
            "AI",
            "robotics",
            "cloud",
            "app",
            "platform",
            "network",
            "community",
            "meeting",
            "conference",
            "tutorial",
            "guide"
    };

    /**
     * Starts the mock Twitter stream by generating simulated tweets at fixed intervals.
     * This method retrieves keywords and other configuration data, then calls {@link #simulateTwitterStream}.
     */
    @Override
    public void start() {
        final String[] keywords = twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
        final int minTweetLength = twitterToKafkaServiceConfigData.getMockMinTweetLength();
        final int maxTweetLength = twitterToKafkaServiceConfigData.getMockMaxTweetLength();
        long sleepTimeMs = twitterToKafkaServiceConfigData.getMockSleepMs();
        log.info("Starting mock filtering twitter streams for keywords {}", Arrays.toString(keywords));
        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTimeMs);
    }

    /**
     * Simulates a Twitter stream by generating JSON-formatted tweets based on the specified keywords,
     * tweet length, and sleep interval, and passes each generated tweet to the Kafka listener.
     *
     * @param keywords        Keywords to include in the simulated tweet content.
     * @param minTweetLength  Minimum length of the generated tweet content.
     * @param maxTweetLength  Maximum length of the generated tweet content.
     * @param sleepTimeMs     Interval in milliseconds between tweet generations.
     */
    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTimeMs) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    String formattedTweetAsRawJson = getFormattedTweet(keywords, minTweetLength, maxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(sleepTimeMs);
                }
            } catch (TwitterException e) {
                log.error("Error creating twitter status!", e);
            }
        });
    }

    /**
     * Causes the current thread to sleep for the specified amount of time.
     * If interrupted, this method resets the interrupt status and throws a custom exception.
     *
     * @param sleepTimeMs the time in milliseconds to sleep
     */
    private void sleep(long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TwitterToKafkaServiceException("Error while sleeping for waiting new status to create!!");
        }
    }

    /**
     * Generates a formatted JSON string representing a simulated tweet with random content,
     * a current timestamp, and a random tweet ID and user ID.
     *
     * @param keywords       Keywords to include in the simulated tweet.
     * @param minTweetLength Minimum tweet content length.
     * @param maxTweetLength Maximum tweet content length.
     * @return A formatted JSON string representing a simulated tweet.
     */
    private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
        String[] params = new String[]{
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatTweetAsJsonWithParams(params);
    }

    /**
     * Populates the tweet JSON template with specified parameters, including timestamp, ID, content, and user ID.
     *
     * @param params The array of parameters to insert into the tweet JSON.
     * @return A JSON string of the tweet with placeholders replaced by the given parameters.
     */
    private String formatTweetAsJsonWithParams(String[] params) {
        String tweet = TWEET_AS_RAW_JSON;

        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    /**
     * Generates random tweet content by appending random words and inserting keywords at random positions.
     *
     * @param keywords       Keywords to include in the tweet.
     * @param minTweetLength Minimum length of the tweet content.
     * @param maxTweetLength Maximum length of the tweet content.
     * @return Randomly generated tweet content as a string.
     */
    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
        StringBuilder tweet = new StringBuilder();
        int tweetLength = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
        return constructRandomTweet(keywords, tweet, tweetLength);
    }

    /**
     * Constructs a random tweet by appending words from a predefined array and inserting keywords.
     *
     * @param keywords   Array of keywords to include in the tweet content.
     * @param tweet      StringBuilder object to construct the tweet content.
     * @param tweetLength The desired length of the tweet content.
     * @return Randomly constructed tweet content as a string.
     */
    private String constructRandomTweet(String[] keywords, StringBuilder tweet, int tweetLength) {
        for (int i = 0; i < tweetLength; i++) {
            tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if (i == tweetLength / 2) {
                tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweet.toString().trim();
    }

}
