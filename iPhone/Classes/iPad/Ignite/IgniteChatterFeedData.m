//
//  IgniteChatterFeedData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/31/12.
//  Copyright 2012 Concur. All rights reserved.
//

#import "IgniteChatterFeedData.h"
#import "BaseManager.h"
#import "ConcurMobileAppDelegate.h"
#import "EntityChatterFeed.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"
#import "EntityChatterCommentEntry.h"
#import "EntityChatterAttachment.h"
#import "ExSystem.h"
#import "DateTimeFormatter.h"
#import "DataConstants.h"
#import "SalesForceUserManager.h"

@interface IgniteChatterFeedData ()
-(void) saveContext;
-(void) parseData:(NSData *)data forFeed:(EntityChatterFeed*)chatterFeed;
-(void) parseFeedEntry:(NSDictionary*)feedEntry intoEntity:(EntityChatterFeedEntry*)entity;
-(void) parseEntry:(NSDictionary*)entry intoEntity:(EntityChatterAbstractEntry*)entity;
-(NSString*) getNonEmptyString:(NSObject*)obj;
-(NSDictionary*) getNullableDictionary:(NSObject*)obj;
-(NSNumber*) getNumber:(NSObject*)obj withDefault:(int)defaultInt;
@end

@implementation IgniteChatterFeedData

@synthesize managedObjectContext=__managedObjectContext;
@synthesize itemId, feedLabel, photoDict;

#pragma mark - MsgResponder Overrides

-(NSString *)getMsgIdKey
{
	return CHATTER_FEED_DATA;
}

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.itemId = [parameterBag objectForKey:@"ITEM_ID"];
 
    self.feedLabel = CHATTER_NEWS_FEED_LABEL;
    self.photoDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

    NSString *path = [NSString stringWithFormat:@"%@/services/data/v25.0/chatter/feeds/news/me/feed-items", [[SalesForceUserManager sharedInstance] getInstanceUrl]];
    if ([itemId length]) {
        path = [NSString stringWithFormat:@"%@/services/data/v25.0/chatter/feed-items/%@/comments", [[SalesForceUserManager sharedInstance] getInstanceUrl], self.itemId];
    }

	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(void) preloadPhotos
{
    if (photoDict == nil)
        return;
    
    NSArray *cacheNames = photoDict.allKeys;
    for (NSString *cacheName in cacheNames)
    {
        NSString *url = photoDict[cacheName];
        
        // Passing nil for IV (image view) and MVC.  The image will be cached.
        [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:url RespondToImage:nil IV:nil MVC:nil ImageCacheName:cacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
    }
}

-(void) respondToXMLData:(NSData *)data withMsg:(Msg *)msg// It's JSON, not XML!
{
    if (msg.responseCode < 200 || msg.responseCode >= 300)
	{
		return; // Return instead of parsing failed responses.
	}

	// Find the existing feed (if any)
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityChatterFeed" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(label = %@)", self.feedLabel];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    
    // Look for an existing feed.
    EntityChatterFeed *chatterFeed = nil;
    if(aFetch != nil && [aFetch count] > 0)
	{
        chatterFeed = aFetch[0];
	}
	
    // If there's not already a feed, then create one.
    if (chatterFeed == nil)
    {
        chatterFeed = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterFeed" inManagedObjectContext:self.managedObjectContext];
        [chatterFeed setValue:self.feedLabel forKey:@"label"];
    }
    
    // Delete existing feed entries
    NSArray *entriesArray = [chatterFeed.entries allObjects];
    for(EntityChatterAbstractEntry *entry in entriesArray)
        [self.managedObjectContext deleteObject:entry];
    
    // Parse
    [self parseData:data forFeed:chatterFeed];

    // Save the changes
    [self saveContext];
}

#pragma mark - Parsing
-(void) parseData:(NSData *)data forFeed:(EntityChatterFeed*)chatterFeed
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
        
        NSString *errorMessage = [NSString stringWithFormat:@"IgniteChatterFeedData::respondToXMLData: Error code = %li, domain = %@, description = %@, failure reason = %@", (long)error.code, errorDomain, localizedDescription, localizedFailureReason];
        
        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return;
    }
    
    // Expecting top-level JSON to be a dictionary
    if (json != nil && [json isKindOfClass:[NSDictionary class]])
    {
        // An 'items' key should be in the dictionary
        NSDictionary *items = (NSDictionary*)json;
        if (items != nil && [items isKindOfClass:[NSDictionary class]])
        {
            // The value of the 'items' key is an array of feed items.
            NSArray *feedItems = items[@"items"];
            
            if (feedItems != nil && [feedItems isKindOfClass:[NSArray class]])
            {
                for (NSDictionary *feedItem in feedItems)
                {
                    EntityChatterFeedEntry *feedEntryEntity = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterFeedEntry" inManagedObjectContext:self.managedObjectContext];
                     [self parseFeedEntry:feedItem intoEntity:feedEntryEntity];
                    [chatterFeed addEntriesObject:feedEntryEntity];
                }
            }
        }
        else
        {
            [[MCLogging getInstance] log:@"IgniteChatterFeedData::respondToXMLData: Expected array of feed items" Level:MC_LOG_ERRO];
        }
    }
    else
    {
        [[MCLogging getInstance] log:@"IgniteChatterFeedData::respondToXMLData: Expected array of items" Level:MC_LOG_ERRO];
    }
}

-(void) parseFeedEntry:(NSDictionary*)feedEntry intoEntity:(EntityChatterFeedEntry*)feedEntryEntity
{
    [self parseEntry:feedEntry intoEntity:feedEntryEntity];
    
    NSDictionary *commentsDict = feedEntry[@"comments"];
    NSArray *comments = (commentsDict != nil ? commentsDict[@"comments"] : nil);

    if (comments != nil)
    {
        for (NSDictionary* commentEntry in comments)
        {
            EntityChatterCommentEntry *commentEntryEntity = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterCommentEntry" inManagedObjectContext:self.managedObjectContext];
            [self parseEntry:commentEntry intoEntity:commentEntryEntity];
            [feedEntryEntity addCommentsObject:commentEntryEntity];
        }
    }
}

-(void) parseEntry:(NSDictionary*)entry intoEntity:(EntityChatterAbstractEntry*)entity
{
    entity.identifier = entry[@"id"];
    
    NSString *createdDateStr = entry[@"createdDate"];
    entity.createdDate = [DateTimeFormatter getISO8601Date:createdDateStr];
    
    NSDictionary *bodyDict = entry[@"body"];
    entity.text = [self getNonEmptyString:bodyDict[@"text"]];
    
    NSDictionary *likesDict = entry[@"likes"];
    entity.totalLikes = likesDict[@"total"];
    
    NSDictionary *parentDict = [self getNullableDictionary:entry[@"parent"]];
    if (parentDict != nil)
    {
        NSString *parentId = [self getNonEmptyString:parentDict[@"id"]];
        if (parentId.length > 0)
            entity.parentIdentifier = parentId;
    }
    
    NSDictionary *authorDict = entry[@"actor"];
    if (authorDict == nil)
        authorDict = entry[@"user"];
    
    EntityChatterAuthor *author = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterAuthor" inManagedObjectContext:self.managedObjectContext];
    entity.author = author;
    author.identifier = [self getNonEmptyString:authorDict[@"id"]];
    author.name = [self getNonEmptyString:authorDict[@"name"]];
    author.companyName = [self getNonEmptyString:authorDict[@"companyName"]];
    
    NSDictionary *authorPhotoDict = authorDict[@"photo"];
    NSString *smallPhotoUrl = [self getNonEmptyString:authorPhotoDict[@"smallPhotoUrl"]];
    author.smallPhotoUrl = smallPhotoUrl;
    if (smallPhotoUrl != nil && smallPhotoUrl.length > 0)
    {
        NSString* cacheName = [NSString stringWithFormat:@"Photo_Small_%@", author.identifier];
        photoDict[cacheName] = smallPhotoUrl;
    }
    
    NSDictionary *attachmentDict = [self getNullableDictionary:entry[@"attachment"]];
    if (attachmentDict != nil)
    {
        EntityChatterAttachment *attachment = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterAttachment" inManagedObjectContext:self.managedObjectContext];
        entity.relAttachment = attachment;
        
        attachment.identifier = [self getNonEmptyString:attachmentDict[@"id"]];
        attachment.title = [self getNonEmptyString:attachmentDict[@"title"]];
        attachment.downloadUrl = [self getNonEmptyString:attachmentDict[@"url"]];
        attachment.hasImagePreview = [self getNumber:attachmentDict[@"hasImagePreview"] withDefault:0];
    }
}

-(NSString*) getNonEmptyString:(NSObject*)obj
{
    return ((obj != nil && [obj isKindOfClass:[NSString class]]) ? (NSString*)obj : @"");
}

-(NSDictionary*) getNullableDictionary:(NSObject*)obj
{
    return ((obj != nil && [obj isKindOfClass:[NSDictionary class]]) ? (NSDictionary*)obj : nil);
}

-(NSNumber*) getNumber:(NSObject*)obj withDefault:(int)defaultInt
{
    return ((obj != nil && [obj isKindOfClass:[NSNumber class]]) ? (NSNumber*)obj : @(defaultInt));
}

#pragma mark - Context
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = __managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"IgniteChatterFeedData: Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}

#pragma mark - End of Lifecycle

@end
