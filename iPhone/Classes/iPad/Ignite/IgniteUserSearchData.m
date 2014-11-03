//
//  IgniteUserSearchData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteUserSearchData.h"
#import "SalesForceUserManager.h"
#import "IgniteUserSearchResult.h"


@interface IgniteUserSearchData (private)
-(void) parseData:(NSData *)data;
@end


@implementation IgniteUserSearchData

@synthesize searchString, searchResults;

#pragma mark - MsgResponder Overrides

-(NSString *)getMsgIdKey
{
	return IGNITE_SEARCH_USERS;
}

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.searchString = [parameterBag objectForKey:@"SEARCH_STRING"];
    self.searchResults = [[NSMutableArray alloc] initWithObjects:nil];
    
    NSString *encodedSearchString = [self.searchString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];

    NSString *path = [NSString stringWithFormat:@"%@/services/data/v25.0/chatter/users?q=%@*", [[SalesForceUserManager sharedInstance] getInstanceUrl], encodedSearchString];
 	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(void) respondToXMLData:(NSData *)data withMsg:(Msg *)msg // It's JSON, not XML!
{
    if (msg.responseCode < 200 || msg.responseCode >= 300)
	{
		return; // Return instead of parsing failed responses.
	}
    
    [self parseData:data];
}

#pragma mark - Parsing
-(void) parseData:(NSData *)data
{
    // Deserialize data into JSON
    NSError *error = nil;
    NSObject *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
    
    // Handle error deserializing into JSON
    if (error != nil)
    {
        NSString *errorDomain = (error.domain == nil ? @"" : error.domain);
        NSString *localizedDescription = (error.localizedDescription == nil ? @"": error.localizedDescription);
        NSString *localizedFailureReason = (error.localizedFailureReason == nil ? @"" : error.localizedFailureReason);
        
        NSString *errorMessage = [NSString stringWithFormat:@"IgniteUserSearchData::respondToXMLData: Error code = %i, domain = %@, description = %@, failure reason = %@", error.code, errorDomain, localizedDescription, localizedFailureReason];
        
        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return;
    }
    
    // Expecting top-level JSON to be a dictionary
    if (json != nil && [json isKindOfClass:[NSDictionary class]])
    {
        // A 'users' key should be in the dictionary
        NSDictionary *items = (NSDictionary*)json;
        if (items != nil && [items isKindOfClass:[NSDictionary class]])
        {
            // The value of the 'records' key is an array of users.
            NSArray *users = [items objectForKey:@"users"];
            
            if (users != nil && [users isKindOfClass:[NSArray class]])
            {
                // Go through each user in the array of users
                for (NSDictionary *user in users)
                {
                    IgniteUserSearchResult *searchResult = [[IgniteUserSearchResult alloc] init];
                    searchResult.identifier = [user objectForKey:@"id"];
                    searchResult.name = [user objectForKey:@"name"];
                    searchResult.userName = [user objectForKey:@"username"];
                    [self.searchResults addObject:searchResult];
                }
            }
            else
            {
                [[MCLogging getInstance] log:@"IgniteUserSearchData::parseData: Expected array of users" Level:MC_LOG_ERRO];
            }
        }
        else
        {
            [[MCLogging getInstance] log:@"IgniteUserSearchData::parseData: Expected dictionary with users key" Level:MC_LOG_ERRO];
        }
    }
    else
    {
        [[MCLogging getInstance] log:@"IgniteUserSearchData::parseData: Expected dictionary" Level:MC_LOG_ERRO];
    }
}


@end
