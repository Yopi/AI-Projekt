#require 'json'
#cb = JSON.parse(File.open('../data/cb.json').read)
tweets = File.open('../data/corpus/pg1342.txt').read
URI_REGEX = %r"((?:(?:[^ :/?#]+):)(?://(?:[^ /?#]*))(?:[^ ?#]*)(?:\?(?:[^ #]*))?(?:#(?:[^ ]*))?)"
#cb['tweets'].map! do |tweet|
#  tweet.gsub("\n\r", '')
#end
#tweets = cb['tweets'].join("\n")
#tweets.gsub!(URI_REGEX, '')
#tweets.gsub!(/(@\w+)/, '')
#tweets.gsub!(/(#\w+)/, '')
#tweets.gsub!("@", '')
#tweets.gsub!("&amp;", 'and')
#tweets.gsub!(":", '')
#tweets.gsub!(/[!]+/, '!')
#tweets.gsub!(/[?]+/, '?')
#tweets.gsub!(/[.]+/, '.')
#tweets.gsub!(" .", '.')
tweets.gsub!("Mr.", "mister")
tweets.gsub!("Mrs.", "miss")
tweets.gsub!(";", ',')
tweets.gsub!("\"", '')
tweets.gsub!("''", '')
tweets.gsub!("``", '')
tweets.gsub!("(", '')
tweets.gsub!(")", '')
tweets.gsub!("_", '')
tweets.gsub!("*", '')
tweets.gsub!(/\s+/, " ")
tweets.gsub!(". ", ".\n")
tweets.downcase!
puts tweets
