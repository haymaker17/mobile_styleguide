//
//  IgniteUserInfoData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteUserInfoData.h"

@interface IgniteUserInfoData (Private)
- (void) deleteExistingUsers;
- (void)saveContext;
@end

@implementation IgniteUserInfoData

@synthesize managedObjectContext=__managedObjectContext;
@synthesize user;

@synthesize accessToken, instanceUrl, profileUserId, profileName, profileSmallPhotoUrl;

#pragma mark - MsgResponder Overrides

-(NSString *)getMsgIdKey
{
	return IGNITE_USER_INFO_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.path = [NSString stringWithFormat:@"%@/mobile/SalesForce/GetUserInfo",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

#pragma mark - Parsing methods
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"SalesForceUserInfo"])
	{
        // Delete the old users in core data.
        [self deleteExistingUsers];
        
        // Add a new user to core data.
        self.user = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySalesForceUser" inManagedObjectContext:[self managedObjectContext]];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"Name"])
	{
        self.user.name = buildString;
	}
    else if ([currentElement isEqualToString:@"SmallPhotoUrl"])
	{
        self.user.smallPhotoUrl = buildString;
	}
    else if ([currentElement isEqualToString:@"UserId"])
	{
        self.user.identifier = buildString;
	}
    else if ([currentElement isEqualToString:@"AccessToken"])
	{
        self.user.accessToken = buildString;
	}
    else if ([currentElement isEqualToString:@"InstanceUrl"])
	{
        self.user.instanceUrl = buildString;
	}
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    [self saveContext];
    __managedObjectContext = nil;
}

#pragma mark - Util
-(void) deleteExistingUsers // The caller is responsible for saving the changes to core data
{
    // Find the existing feed (if any)
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesForceUser" inManagedObjectContext:[self managedObjectContext]];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [[self managedObjectContext] executeFetchRequest:fetchRequest error:&error];
    // TODO: Handle error
    
	// Delete old users (if any)
    for(EntitySalesForceUser *existingUser in aFetch)
        [[self managedObjectContext] deleteObject:existingUser];
}

#pragma mark - Context
/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
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
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}

#pragma mark - Lifecycle
@end
