//
//  GovExpenseTypesData.m
//  ConcurMobile
//
//  Created by ernest cho on 9/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseTypesData.h"
#import "Msg.h"
#import "GovExpenseType.h"

@implementation GovExpenseTypesData

@synthesize currentExpenseType, expenseTypes;

-(id)init
{
	self = [super init];
    if (self) {
        self.expenseTypes = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
	return self;
}

-(NSString *) getMsgIdKey
{
    return GET_GOV_EXPENSE_TYPES;
}

// the descriptions are the keys
-(NSArray*) getDescriptions
{
    return [expenseTypes allKeys];
}

-(GovExpenseType*) getExpenseFor:(NSString*)description
{
    return expenseTypes[description];
}

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMExpenseTypes/",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:GET_GOV_EXPENSE_TYPES State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    self.currentElement = elementName;
    if ([elementName isEqualToString:@"ExpenseListRow"]) {
        self.currentExpenseType = [[GovExpenseType alloc] init];
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"ExpenseListRow"])
    {
        expenseTypes[currentExpenseType.expenseDescription] = currentExpenseType;
        self.currentExpenseType = nil;
    }
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"ExpenseDesc"])
    {
        currentExpenseType.expenseDescription = buildString;
    }
}

@end
