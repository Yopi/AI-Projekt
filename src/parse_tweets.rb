require 'json'
cb = JSON.parse(File.open('../data/cb.json').read)

URI_REGEX = %r"((?:(?:[^ :/?#]+):)(?://(?:[^ /?#]*))(?:[^ ?#]*)(?:\?(?:[^ #]*))?(?:#(?:[^ ]*))?)"
cb['tweets'].map! do |tweet|
  tweet.gsub("\n\r", '')
end
tweets = cb['tweets'].join("\n")
tweets.gsub!(/[_]/, '')
tweets.gsub!(URI_REGEX, '')
puts tweets
