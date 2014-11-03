//
//  GovWarningMessagesData.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovWarningMessagesData.h"
#import "BaseManager.h"

@implementation GovWarningMessagesData
@synthesize warningMessages;
@synthesize managedObjectContext=__managedObjectContext;

-(NSString *)getMsgIdKey
{
    return GOV_WARNING_MSG;
}

-(void)flushData
{
    [super flushData];
    __managedObjectContext = nil;
}

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/MobileSession/GovWarningMessages/",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:GOV_WARNING_MSG State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

-(void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    [BaseManager deleteAll:@"EntityWarningMessages" withContext:[self managedObjectContext]];
    self.warningMessages = (EntityWarningMessages*)[BaseManager makeNew:@"EntityWarningMessages" withContext:[self managedObjectContext]];
}

-(void)parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    
    [self saveContext];
    __managedObjectContext = nil;
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"behaviorTitle"])
	{
		[self.warningMessages setBehaviorTitle:buildString];
	}
    else if ([currentElement isEqualToString:@"behaviorText"])
    {
        [self.warningMessages setBehaviorText:buildString];
    }
    else if ([currentElement isEqualToString:@"privacyTitle"])
    {
        [self.warningMessages setPrivacyTitle:buildString];
    }
    else if ([currentElement isEqualToString:@"privacyText"])
    {
		[self.warningMessages setPrivacyText:buildString];
    }
    else if ([currentElement isEqualToString:@"privacyTextShort"])
    {
		[self.warningMessages setPrivacyTextShort:buildString];
    }
    else if ([currentElement isEqualToString:@"warningTitle"])
    {
		[self.warningMessages setWarningTitle:buildString];
    }
    else if ([currentElement isEqualToString:@"warningText"])
    {
		[self.warningMessages setWarningText:buildString];
    }
    else if ([currentElement isEqualToString:@"warningTextShort"])
    {
		[self.warningMessages setWarningTextShort:buildString];
    }
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
@end
