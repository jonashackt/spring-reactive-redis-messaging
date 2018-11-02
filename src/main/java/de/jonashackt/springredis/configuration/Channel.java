package de.jonashackt.springredis.configuration;

public enum Channel {

    COFFEES("coffees-channel-topic");

    private String channelTopicName;

    Channel(String channelTopicName) {
        this.channelTopicName = channelTopicName;
    }

    public String topicName() {
        return channelTopicName;
    }
}
