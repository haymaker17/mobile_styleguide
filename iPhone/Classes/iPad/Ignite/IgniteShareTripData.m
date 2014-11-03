//
//  IgniteShareTripData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteShareTripData.h"
#import "DataConstants.h"
#import "SalesForceUserManager.h"


@interface IgniteShareTripData (private)
-(NSString*) makeBodyWithCommment:(NSString*)comment toRecipient:(NSString*)recipientId;
@end


@implementation IgniteShareTripData

-(NSString *)getMsgIdKey
{
	return SALESFORCE_SHARE_TRIP_DATA;
}

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    NSString *salesforceTripId = [parameterBag objectForKey:@"SALESFORCE_TRIP_ID"];
    NSString *recipientId = [parameterBag objectForKey:@"RECIPIENT_ID"];
    NSString *comment = [parameterBag objectForKey:@"COMMENT"];
    
    NSString *path = [NSMutableString stringWithFormat:@"%@/services/data/v23.0/chatter/feeds/record/%@/feed-items", [[SalesForceUserManager sharedInstance] getInstanceUrl], salesforceTripId];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"POST"];
    [msg setContentType:@"application/json"];
    [msg setBody:[self makeBodyWithCommment:comment toRecipient:recipientId]];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(NSString*) makeBodyWithCommment:(NSString*)comment toRecipient:(NSString*)recipientId
{
    NSString *jsonTemplate = @"{\"body\":{\"messageSegments\":[{\"type\":\"mention\",\"id\":\"%@\"},{\"type\":\"Text\",\"text\":\" %@\"}]}}";
    return [NSString stringWithFormat:jsonTemplate, recipientId, comment];
}


@end
