//
//  OAuthConsumer.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <CommonCrypto/CommonHMAC.h>
#import "OAuthConsumer.h"

@implementation OAuthConsumer

#pragma mark - Overrides

-(NSString*) getToken
{
	return @"";
}

-(NSString*) getTokenSecret
{
	return @"";
}

#pragma mark - Implementation

-(NSString*) getTimeStamp
{
	NSTimeInterval timeSinceEpoch = [[NSDate date] timeIntervalSince1970];
	return [NSString stringWithFormat:@"%ld", (long)timeSinceEpoch];
}

-(NSString*) getNonce
{
	const int bufferLen = 40;
	unsigned char buffer[bufferLen];
	for (int i = 0; i < bufferLen; i++)
	{
		buffer[i] = (arc4random() % 10) + 48;	// Ascii value in the range 48 ('0') to 57 ('9')
	}
	__autoreleasing NSString* result = [[NSString alloc] initWithBytes:buffer length:bufferLen encoding:NSASCIIStringEncoding];
    return result;
}

-(NSString*) urlEncodeValue:(NSString*)str
{
	__autoreleasing NSString *result = (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(
																		   NULL,
																		   (CFStringRef)str,
																		   NULL,
																		   (CFStringRef)@"!*'();:@&=+$,/?%#[]",
																		   kCFStringEncodingUTF8 ));
	return result;
}

-(NSString*) computeHashWithKey:(NSString *)key data:(NSString*)data
{
	const char *cKey  = [key cStringUsingEncoding:NSUTF8StringEncoding];
	const char *cData = [data cStringUsingEncoding:NSUTF8StringEncoding];
	
	unsigned char cHMAC[CC_SHA1_DIGEST_LENGTH]= {0};
	
	CCHmac(kCCHmacAlgSHA1, cKey, strlen(cKey), cData, strlen(cData), cHMAC);
	
	NSData *HMAC = [NSData dataWithBytes:cHMAC length:sizeof(cHMAC)];
	
	return [HMAC base64String];
}

-(NSString*) oauthHeaderLegs:(int)legs httpMethod:(NSString*)httpMethod url:(NSString*)url
{
	//	Sample signature base string from http://blog.andydenmark.com/2009/03/how-to-build-oauth-consumer.html
	//	GET&https%3A%2F%2Fapi.tripit.com%2Fv1%2Flist%2Ftrip&oauth_consumer_key%3D5dbf348aa966c5f7f07e8ce2ba5e7a3badc234bc%26oauth_nonce%3D720201c8b047528e4fcc119a98cffc8e%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1235272610%26oauth_token%3Db865676c95c736c4cbb90c652cd896dec022ba86%26oauth_token_secret%3Df3ce720ebb4c0ddc3469662c246f78ef4d99f1cb%26oauth_version%3D1.0
	
	//	Sample oauth header from http://blog.andydenmark.com/2009/03/how-to-build-oauth-consumer.html (with oauth_token_secret removed)
	//	Authorization: OAuth realm="https://api.tripit.com/v1/list/trip",oauth_nonce="720201c8b047528e4fcc119a98cffc8e",oauth_timestamp="1235272610",oauth_consumer_key="5dbf348aa966c5f7f07e8ce2ba5e7a3badc234bc",oauth_signature_method="HMAC-SHA1",oauth_version="1.0",oauth_token="b865676c95c736c4cbb90c652cd896dec022ba86",oauth_signature="QlHuyGIbtPNNra7qBEAcdbqQuGc%3D"
	
	// When users type in a URL on the iPhone, the first character is often capitalized.
	// Lower-case is needed because URLs are case-sensitive in OAuth.
	if ([url characterAtIndex:0] == 'H')
		url = [NSString stringWithFormat:@"h%@", [url substringFromIndex:1]];
	
    NSString *a = [[MCLogging getInstance] getMessageForField:@"Validation error"];
    NSString *b = [[MCLogging getInstance] getMessageForField:@"Bounds error"];
	NSString *oauth_consumer_key = a;
	NSString *oauth_consumer_secret = b;
	NSString *oauth_nonce = [self getNonce];
	NSString *oauth_signature_method = @"HMAC-SHA1";
	NSString *oauth_timestamp = [self getTimeStamp];
	NSString *oauth_token = [self getToken];
	NSString *oauth_token_secret = (legs == 3 ? [self getTokenSecret] : @"");
	NSString *oauth_version = @"1.0";
	
	NSString *encodedToken = (oauth_token == nil ? @"" : [self urlEncodeValue:oauth_token]);

	NSString* suffix = nil;
	if (legs == 3)
	{
		suffix = [NSString stringWithFormat:@"oauth_consumer_key=%@&oauth_nonce=%@&oauth_signature_method=%@&oauth_timestamp=%@&oauth_token=%@&oauth_version=%@", oauth_consumer_key, oauth_nonce, oauth_signature_method, oauth_timestamp, encodedToken, oauth_version];
	}
	else if (legs == 2)
	{
		suffix = [NSString stringWithFormat:@"oauth_consumer_key=%@&oauth_nonce=%@&oauth_signature_method=%@&oauth_timestamp=%@&oauth_version=%@", oauth_consumer_key, oauth_nonce, oauth_signature_method, oauth_timestamp, oauth_version];
	}
	
	NSString* encodedUrl = [self urlEncodeValue:url];
	NSString* encodedSuffix = [self urlEncodeValue:suffix];
	NSString* signatureBase = [NSString stringWithFormat:@"%@&%@&%@", httpMethod, encodedUrl, encodedSuffix];
	
	NSString *encodedConsumerSecret = [self urlEncodeValue:oauth_consumer_secret];
	NSString *encodedTokenSecret = (oauth_token_secret == nil ? @"" : [self urlEncodeValue:oauth_token_secret]);
	NSString *hashKey = [NSString stringWithFormat:@"%@&%@", encodedConsumerSecret, encodedTokenSecret];
	
	NSString *signature = [self computeHashWithKey:hashKey data:signatureBase];
	NSString *encodedSignature = [self urlEncodeValue:signature];
	
	//NSString *encodedConsumerKey = [self urlEncodeValue:oauth_consumer_key];
	
	NSString *header = nil;
	if (legs == 3)
	{
		header = [NSString stringWithFormat:@"OAuth realm=\"%@\",oauth_nonce=\"%@\",oauth_timestamp=\"%@\",oauth_consumer_key=\"%@\",oauth_signature_method=\"%@\",oauth_version=\"%@\",oauth_token=\"%@\",oauth_signature=\"%@\"", url, oauth_nonce, oauth_timestamp,  oauth_consumer_key, oauth_signature_method, oauth_version, encodedToken, encodedSignature];
	}
	else if (legs == 2)
	{
		header = [NSString stringWithFormat:@"OAuth realm=\"%@\",oauth_nonce=\"%@\",oauth_timestamp=\"%@\",oauth_consumer_key=\"%@\",oauth_signature_method=\"%@\",oauth_version=\"%@\",oauth_signature=\"%@\"", url, oauth_nonce, oauth_timestamp, oauth_consumer_key, oauth_signature_method, oauth_version, encodedSignature];
	}
	
	return header;
}

@end
