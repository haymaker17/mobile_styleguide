//
//  ChatterCommentsMsg.m
//  ConcurMobile
//
//  Created by ernest cho on 7/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterCommentsMsg.h"
#import "SalesForceUserManager.h"
#import "EntityChatterFeed.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterCommentEntry.h"
#import "EntityChatterAuthor.h"
#import "EntityChatterAttachment.h"

@interface ChatterCommentsMsg()
@property (nonatomic, readwrite, strong) NSString *itemId;
@property (nonatomic, readwrite, strong) NSString *feedLabel;
@property (nonatomic, readwrite, strong) NSManagedObjectContext  *managedObjectContext;
@property (nonatomic, readwrite, strong) NSMutableDictionary *photoDict; // Key = cache name, Value = url
@end

@implementation ChatterCommentsMsg

-(NSString *)getMsgIdKey
{
	return CHATTER_COMMENTS_DATA;
}

// this message requires that the ITEM_ID be set correctly, if not it will fail with a network error.
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.feedLabel = CHATTER_NEWS_FEED_LABEL;
    self.photoDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

    self.itemId = [parameterBag objectForKey:@"ITEM_ID"];
    NSString *path = [NSString stringWithFormat:@"%@/services/data/v25.0/chatter/feed-items/%@/comments", [[SalesForceUserManager sharedInstance] getInstanceUrl], self.itemId];
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(void) respondToXMLData:(NSData *)data withMsg:(Msg *)msg// It's JSON, not XML!
{
    if (msg.responseCode < 200 || msg.responseCode >= 300)
	{
		return; // Return instead of parsing failed responses.
	}

    // make sure we already have an entry for this itemId
    NSError *error;
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityChatterFeedEntry" inManagedObjectContext:self.managedObjectContext];

    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(identifier = %@)", self.itemId];
    [fetchRequest setPredicate:predicate];
 
    [fetchRequest setEntity:entity];

    NSArray *temp = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    if (temp != nil && temp.count == 1) {
        EntityChatterFeedEntry *firstPost = (EntityChatterFeedEntry *)temp[0];

        // remove the comments for this post
        NSArray *entriesArray = [firstPost.comments allObjects];
        for(EntityChatterCommentEntry *comment in entriesArray) {
            [self.managedObjectContext deleteObject:comment];
        }

        // add comments to this post
        [self parseData:data intoFeedEntry:firstPost];
        
        // Save the changes
        [self saveContext];
    }
}

#pragma mark - Parsing
-(void) parseData:(NSData *)data intoFeedEntry:(EntityChatterFeedEntry *)feedEntry
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

        NSString *errorMessage = [NSString stringWithFormat:@"ChatterCommentsMsg:respondToXMLData: Error code = %i, domain = %@, description = %@, failure reason = %@", error.code, errorDomain, localizedDescription, localizedFailureReason];

        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return;
    }

    // Expecting top-level JSON to be a dictionary
    if (json != nil && [json isKindOfClass:[NSDictionary class]])
    {
        // An 'comments' key should be in the dictionary
        NSDictionary *root = (NSDictionary*)json;
        if (root != nil && [root isKindOfClass:[NSDictionary class]])
        {
            // The value of the 'comments' key is an array of feed items.
            NSArray *comments = root[@"comments"];

            if (comments != nil && [comments isKindOfClass:[NSArray class]])
            {
                for (NSDictionary *comment in comments)
                {
                    EntityChatterCommentEntry *commentEntryEntity = [NSEntityDescription insertNewObjectForEntityForName:@"EntityChatterCommentEntry" inManagedObjectContext:self.managedObjectContext];
                    [self parseEntry:comment intoEntity:commentEntryEntity];
                    [feedEntry addCommentsObject:commentEntryEntity];
                }
            }
        }
        else
        {
            [[MCLogging getInstance] log:@"ChatterCommentsMsg::respondToXMLData: Expected array of feed items" Level:MC_LOG_ERRO];
        }
    }
    else
    {
        [[MCLogging getInstance] log:@"ChatterCommentsMsg::respondToXMLData: Expected array of items" Level:MC_LOG_ERRO];
    }
}

// this stuff is all copied from ignite code
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
        self.photoDict[cacheName] = smallPhotoUrl;
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
    if (_managedObjectContext != nil)
    {
        return _managedObjectContext;
    }

    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        _managedObjectContext = [[NSManagedObjectContext alloc] init];
        [_managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return _managedObjectContext;
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.

             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"ChatterCommentsMsg: Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }
}

@end
