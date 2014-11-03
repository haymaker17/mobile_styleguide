//
//  GovUnappliedExpensesData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovUnappliedExpensesData.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "BaseManager.h"

@implementation GovUnappliedExpensesData
@synthesize currentExpense;
@synthesize managedObjectContext=__managedObjectContext;

-(NSString *) getMsgIdKey
{
    return GOV_UNAPPLIED_EXPENSES;
}

-(BOOL) shouldParseCachedData
{
    return NO;
}

-(void) flushData
{
    [super flushData];
    self.currentExpense = nil;
    __managedObjectContext = nil;
}

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMUnappliedExpenses/",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:GOV_UNAPPLIED_EXPENSES State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    // Populate the existing list of all expenses
    [BaseManager deleteAll:@"EntityGovExpense" withContext:[self managedObjectContext]];
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    
    [self saveContext];
    __managedObjectContext = nil;
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    self.currentElement = elementName;
    if ([elementName isEqualToString:@"MobileExp"]) {
        self.currentExpense = (EntityGovExpense*)[BaseManager makeNew:@"EntityGovExpense" withContext:[self managedObjectContext]];
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"MobileExp"])
    {
        self.currentExpense = nil;
    }
}

/*
 <tran_date>2012-01-27</tran_date>
 <posted_amt>10.00</posted_amt>
 <tran_description>Airport Tax - hahahaha</tran_description>
 <ccexpid>20120127000049310551.00</ccexpid>
 <imageid>1FFCB5A569B137ECB26B3A210F6ADE4A</imageid>
*/
-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"tran_date"])
	{
		[self.currentExpense setExpDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"ccexpid"])
    {
		[self.currentExpense setCcExpId:buildString];
    }
    else if ([currentElement isEqualToString:@"imageid"])
    {
		[self.currentExpense setImageId:buildString];
    }
    else if ([currentElement isEqualToString:@"posted_amt"])
    {
		[self.currentExpense setAmount:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"tran_description"])
    {
        NSArray * subStrings = [buildString componentsSeparatedByString:@" - "];
        if (subStrings != nil && [subStrings count]>0)
		[self.currentExpense setExpenseDesc:[subStrings objectAtIndex:0]];
    }
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

@end
