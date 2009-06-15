#!/usr/bin/perl
$/ = undef;
$_ = <>;

s/(PJ_DECL.*\s+pjsua_media_transports_attach\s*[^)]+\);)//g;
s/typedef struct pjsua_media_config pjsua_media_config;//g;
print;
