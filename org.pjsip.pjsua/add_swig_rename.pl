#!/usr/bin/perl
$/ = undef;
$_ = <>;

s/PJ_DECL.*\s+(((pjsua|pjmedia)_([A-Za-z1-9_\-]+))\s*\([^)]+\));/%rename($4) $2;\n%javamethodmodifiers $1 "public synchronized";\n$&/g;
print;
