require 'rubygems'
require 'twitter'
require 'json'

require_relative 'twitter_config'
client = Twitter::REST::Client.new do |config|
  config.consumer_key        = CONSUMER_KEY
  config.consumer_secret     = CONSUMER_SECRET
  config.access_token        = ACCESS_TOKEN  
  config.access_token_secret = ACCESS_TOKEN_SECRET
end

tweet_count = 200

if File.exists?('cb.json')
	file = File.open('cb.json', 'r')
	cb_old = JSON.parse(file.read)
	cb = client.user_timeline("carlbildt", {count: tweet_count, max_id: cb_old['id'].to_i - 1, include_rts: false})
	file.close
else
	cb_old = {}
	cb_old['id'] = 999999999
	cb_old['tweets'] = []
	cb = client.user_timeline("carlbildt", {count: tweet_count, include_rts: false})
end

file = File.open('cb.json', 'w+')
tweet_text = cb.map {|tweet| tweet.text}
cb_old['tweets'].concat(tweet_text)
cb_old['id'] = cb.last.id
file.write(JSON.pretty_generate(cb_old))
