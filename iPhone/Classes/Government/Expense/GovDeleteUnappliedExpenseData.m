//
//  GovDeleteUnappliedExpenseData.m
//  ConcurMobile
//
//  Created by charlottef on 1/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDeleteUnappliedExpenseData.h"
#import "EntityGovExpenseExtension.h"

@implementation GovDeleteUnappliedExpenseData

@synthesize ccExpId, status;
@synthesize managedObjectContext=__managedObjectContext;

-(NSString*) getMsgIdKey
{
    return GOV_DELETE_UNAPPLIED_EXPENSE;
}

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag
{
    self.ccExpId = [parameterBag objectForKey:@"EXPENSE_ID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/DeleteTMUnappliedExpense", [ExSystem sharedInstance].entitySettings.uri];
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    
    [msg setBody:[self makeXMLBody]];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	return msg;
}

-(NSString *) makeXMLBody
{
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<DeleteTMUnappliedExpenseRequest>"];
    [bodyXML appendString:[NSString stringWithFormat:@"<ccExpId>%@</ccExpId>", self.ccExpId]];
    [bodyXML appendString:@"</DeleteTMUnappliedExpenseRequest>"];
    return bodyXML;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"ActionStatus"])
	{
        self.status = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"Status"])
	{
		[self.status setStatus:buildString];
	}
    else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		[self.status setErrMsg:buildString];
	}
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"Status"])
    {
        if (self.status != nil && self.status.status != nil && [@"SUCCESS" isEqualToString:self.status.status])
        {
            //NSLog(@"Will add expense on thread: %p with name %@.  Main thread is %p", [NSThread currentThread], [NSThread currentThread].name, [NSThread mainThread]);
            EntityGovExpense *expense = [EntityGovExpense fetchById:self.ccExpId inContext:self.managedObjectContext];
            [expense deleteEntityObject];
        }
    }
}

-(void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [[NSNotificationCenter defaultCenter] addObserver:ad
                                             selector:@selector(processNotification:)
                                                 name:NSManagedObjectContextDidSaveNotification
                                               object:self.managedObjectContext];}

-(void)parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    //Does this need to be sync'd back to the main thread?
    [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
}

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

@end
