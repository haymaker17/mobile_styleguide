//
//  IgniteChatterPostData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterPostData.h"
#import "DataConstants.h"
#import "SalesForceUserManager.h"

@interface IgniteChatterPostData ()
-(NSString*) makeBody:(NSString*)text toRecipient:(NSString*)recipientId;
@end

@implementation IgniteChatterPostData

-(NSString *)getMsgIdKey
{
	return CHATTER_POST_DATA;
}

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    NSString *path = nil;
    NSString* feedEntryIdentifier = [parameterBag objectForKey:@"FEED_ENTRY_IDENTIFIER"];
    NSString* recordId = [parameterBag objectForKey:@"RECORD_ID"];
    // If a feed entry id has been provided, then we are posting a comment for it.  Otherwise,
    // this is a brand new posting.
    if (feedEntryIdentifier != nil)
        path = [NSMutableString stringWithFormat:@"%@/services/data/v25.0/chatter/feed-items/%@/comments", [[SalesForceUserManager sharedInstance] getInstanceUrl], feedEntryIdentifier];
    else if ([recordId length])
        path = [NSMutableString stringWithFormat:@"%@/services/data/v25.0/chatter/feeds/record/%@/feed-items", [[SalesForceUserManager sharedInstance] getInstanceUrl], recordId];
    else
        path = [NSMutableString stringWithFormat:@"%@/services/data/v25.0/chatter/feeds/news/me/feed-items", [[SalesForceUserManager sharedInstance] getInstanceUrl]];
    
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"POST"];
    [msg setContentType:@"application/json"];
    [msg setBody:[self makeBody:[parameterBag objectForKey:@"TEXT"] toRecipient:[parameterBag objectForKey:@"RECIPIENT_ID"]]];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(NSString*) makeBody:(NSString*)text toRecipient:(NSString*)recipientId
{
    NSString *jsonTemplate = nil;
    NSString *body = nil;
    
    if (recipientId != nil && recipientId.length > 0)
    {
        jsonTemplate = @"{\"body\":{\"messageSegments\":[{\"type\":\"mention\",\"id\":\"%@\"},{\"type\":\"Text\",\"text\":\" %@\"}]}}";
        body = [NSString stringWithFormat:jsonTemplate, recipientId, text];
    }
    else
    {
        jsonTemplate = @"{\"body\":{\"messageSegments\":[{\"type\":\"Text\",\"text\":\"%@\"}]}}";
        body = [NSString stringWithFormat:jsonTemplate, text];
   }

    return body;
}

-(void) respondToXMLData:(NSData *)data withMsg:(Msg *)msg// It's JSON, not XML!
{
    if (msg.responseCode != 201)
	{
		return; // Return instead of parsing failed responses.
	}

    // Deserialize data into JSON
    NSError *error = nil;
    NSObject *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];

    // Handle error deserializing into JSON
    if (error != nil)
    {
        NSString *errorDomain = (error.domain == nil ? @"" : error.domain);
        NSString *localizedDescription = (error.localizedDescription == nil ? @"": error.localizedDescription);
        NSString *localizedFailureReason = (error.localizedFailureReason == nil ? @"" : error.localizedFailureReason);

        NSString *errorMessage = [NSString stringWithFormat:@"IgniteChatterPostData::respondToXMLData: Error code = %li, domain = %@, description = %@, failure reason = %@", (long)error.code, errorDomain, localizedDescription, localizedFailureReason];

        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return;
    }

    // Expecting top-level JSON to be a dictionary
    if (json != nil && [json isKindOfClass:[NSDictionary class]])
    {
        NSDictionary *tmp = (NSDictionary *)json;
        self.postId = tmp[@"id"];
    }
}

@end
