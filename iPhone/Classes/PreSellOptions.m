//
//  PreSellOptions.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 20/08/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "PreSellOptions.h"
#import "DeliveryData.h"
#import "CreditCard.h"

@interface PreSellOptions()


@property (nonatomic, strong) NSMutableDictionary		*deliveryOptionItems;
@property (nonatomic, strong) NSMutableArray			*deliveryOptionKeys;

@property (nonatomic, strong) NSXMLParser *dataParser;
@property (nonatomic, strong) NSString					*currentElement;

@property (nonatomic, strong) DeliveryData				*deliveryDataObj;
@property (nonatomic, strong) CreditCard                *creditCardObj;
@property (nonatomic, strong) NSMutableArray            *cancellationPolicyObj;
@property (nonatomic, strong) AffinityProgram           *affinityProgramObj;
@property (nonatomic, strong) PreSellCustomField        *optionItem;
@property (nonatomic, strong) PreSellCustomFieldSelectOption  *optionItemValue;
@property (nonatomic) BOOL isDefault;
@end

@implementation PreSellOptions

-(NSString *)getMsgIdKey
{
	return PRE_SELL_OPTIONS;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/PreSell/PreSellOptions",[ExSystem sharedInstance].entitySettings.uri];
    NSString *choiceId = parameterBag[@"CHOICE_ID"];
    if ([choiceId length])
        path = [path stringByAppendingFormat:@"?choiceId=%@",choiceId];
    path = [path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
    msg.timeoutInterval = 90.0;
	
	return msg;
}

//extracts the XML from a data stream and tells the parser to get parsing
-(void) respondToXMLData:(NSData *)data
{
	self.dataParser = [[NSXMLParser alloc] initWithData:data];
	[self.dataParser setDelegate:self];
	[self.dataParser setShouldProcessNamespaces:YES];
	[self.dataParser setShouldReportNamespacePrefixes:NO];
	[self.dataParser setShouldResolveExternalEntities:NO];
	[self.dataParser parse];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    self.currentElement = elementName;
	
	if ([elementName isEqualToString:@"CreditCardInfo"])
	{
		self.creditCardObj = [[CreditCard alloc] init];
        self.isDefault = NO;
	}
    else if ([elementName isEqualToString:@"TicketDeliveryOption"])
    {
        self.deliveryDataObj = [[DeliveryData alloc] init];
    }
	else if ([elementName isEqualToString:@"TravelProgramChoice"])
    {
        self.affinityProgramObj = [[AffinityProgram alloc] init];
        self.isDefault = NO;
    }
    else if ([elementName isEqualToString:@"OptionItem"])
    {
        self.optionItem = [[PreSellCustomField alloc] init];
    }
    else if ([elementName isEqualToString:@"SelectItem"])
    {
        self.optionItemValue = [[PreSellCustomFieldSelectOption alloc] init];
    }
    else if ([elementName isEqualToString:@"HotelRateCancellationPolicy"])
    {
        self.cancellationPolicyObj = [[NSMutableArray alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"TicketDeliveryOption"] && self.deliveryDataObj)
	{
		if (self.deliveryDataObj.type != nil)
		{
			self.deliveryOptionItems[self.deliveryDataObj.type] = self.deliveryDataObj;
			[self.deliveryOptionKeys addObject:self.deliveryDataObj.type];
		}
        self.deliveryDataObj = nil;
	}
    else if ([elementName isEqualToString:@"CreditCardInfo"] && self.creditCardObj)
    {
        if (self.isDefault)
            [self.creditCards insertObject:self.creditCardObj atIndex:0];
        else
            [self.creditCards addObject:self.creditCardObj];
        self.creditCardObj = nil;
    }
    else if ([elementName isEqualToString:@"TravelProgramChoice"])
    {
        [self.affinityPrograms addObject:self.affinityProgramObj];
        self.affinityProgramObj = nil;
    }
    else if ([elementName isEqualToString:@"OptionItem"])
    {
        [self.optionItems addObject:self.optionItem];
        self.optionItem = nil;
    }
    else if ([elementName isEqualToString:@"SelectItem"])
    {
        if (!self.optionItem.attributeValues)
            self.optionItem.attributeValues = [[NSMutableArray alloc] init];
        [self.optionItem.attributeValues addObject:self.optionItemValue];
        if (self.optionItemValue.isSelected)
        {
            self.optionItem.userInputValue = self.optionItemValue.realValue;
            self.optionItem.userInputValueDisplayText = [self.optionItemValue.displayValue lengthIgnoreWhitespace] ? self.optionItemValue.displayValue : self.optionItemValue.realValue;
        }
        self.optionItemValue = nil;
    }
    else if ([elementName isEqualToString:@"HotelRateCancellationPolicy"])
    {
        self.cancellationPolicyLines = self.cancellationPolicyObj;
        self.cancellationPolicyObj = nil;
    }
    
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"element = %@, string = %@", currentElement, string);
    if (self.deliveryDataObj)
    {
        if ([self.currentElement isEqualToString:@"Fee"])
        {
            [self.deliveryDataObj setFee:string];
        }
        else if ([self.currentElement isEqualToString:@"Name"])
        {
            [self.deliveryDataObj setName:string];
        }
        else if ([self.currentElement isEqualToString:@"Type"])
        {
            [self.deliveryDataObj setType:string];
        }
    }
    else if (self.creditCardObj)
    {
        if ([self.currentElement isEqualToString:@"CreditCardId"])
        {
            self.creditCardObj.ccId = string;
        }
        else if ([self.currentElement isEqualToString:@"CreditCardLastFour"])
        {
            self.creditCardObj.maskedNumber = [@"**" stringByAppendingString:string];
        }
        else if ([self.currentElement isEqualToString:@"Name"])
        {
            self.creditCardObj.name = string;
        }
        else if ([self.currentElement isEqualToString:@"IsDefault"])
        {
            self.isDefault = [string boolValue];
        }
    }
    else if (self.affinityProgramObj)
    {
        if ([self.currentElement isEqualToString:@"AccountNumber"]) {
            self.affinityProgramObj.accountNumber = string;
        }
        else if ([self.currentElement isEqualToString:@"Description"]) {
            self.affinityProgramObj.description = string;
        }
        else if ([self.currentElement isEqualToString:@"ProgramId"]) {
            self.affinityProgramObj.programId = string;
        }
        else if ([self.currentElement isEqualToString:@"ProgramName"]) {
            self.affinityProgramObj.programName = string;
        }
        else if ([self.currentElement isEqualToString:@"ProgramType"]) {
            self.affinityProgramObj.programType = string;
        }
        else if ([self.currentElement isEqualToString:@"Vendor"]) {
            self.affinityProgramObj.vendor = string;
        }
        else if ([self.currentElement isEqualToString:@"VendorAbbrev"]) {
            self.affinityProgramObj.vendorAbbrev = string;
        }
        else if ([self.currentElement isEqualToString:@"ExpectedSelection"] && [string boolValue]) {
            self.defaultProgram = self.affinityProgramObj;
        }    
    }
    else if (self.optionItem)
    {
        if (self.optionItemValue) {
            if ([self.currentElement isEqualToString:@"displayValue"]) {
                self.optionItemValue.displayValue = string;
            }
            else if ([self.currentElement isEqualToString:@"realValue"]) {
                self.optionItemValue.realValue = string;
            }
            else if ([self.currentElement isEqualToString:@"Selected"]) {
                self.optionItemValue.selected = [string boolValue];
            }
        }
        else if ([self.currentElement isEqualToString:@"Remark"]) {
            self.optionItem.title = [self.optionItem.title length] ? [self.optionItem.title stringByAppendingFormat:@" %@",string] : string;
        }
        else if ([self.currentElement isEqualToString:@"UIInputType"]) {
            self.optionItem.dataType = string;
        }
        else if ([self.currentElement isEqualToString:@"Optional"]) {
            self.optionItem.optional = [string boolValue];
        }
        else if ([self.currentElement isEqualToString:@"OptionItemId"]) {
            self.optionItem.itemId = string;
        }
        else if ([self.currentElement isEqualToString:@"TextFieldValue"]) {
            self.optionItem.defaultValue = string;
            self.optionItem.userInputValue = string;
        }
    }
    else if ([self.currentElement isEqualToString:@"IsSuccess"])
    {
        self.isRequestSuccessful = [string isEqualToString:@"true"];
    }
    else if ([self.currentElement isEqualToString:@"CVVNumberRequired"])
    {
        self.isCreditCardCvvRequired = [string isEqualToString:@"true"];
    }
    else if (self.cancellationPolicyObj)
    {
        if ([self.currentElement isEqualToString:@"string"])
        {
            [self.cancellationPolicyObj addObject:string];
        }
    }
}

-(void)parserDidEndDocument:(NSXMLParser *)parser
{
    if ([self.deliveryOptionKeys count])
    {
        self.trainDeliveryData = [[TrainDeliveryData alloc] init];
        self.trainDeliveryData.keys = self.deliveryOptionKeys;
        self.trainDeliveryData.items = self.deliveryOptionItems;
    }
}

-(NSMutableArray *)creditCards
{
    if (!_creditCards)
        _creditCards = [[NSMutableArray alloc] init];
    return _creditCards;
}

-(NSMutableArray *)affinityPrograms
{
    if (!_affinityPrograms)
        _affinityPrograms = [[NSMutableArray alloc] init];
    return _affinityPrograms;
}

-(NSMutableArray *)optionItems
{
    if (!_optionItems)
        _optionItems = [[NSMutableArray alloc] init];
    return _optionItems;
}

-(NSMutableArray *)deliveryOptionKeys
{
    if (!_deliveryOptionKeys)
        _deliveryOptionKeys = [[NSMutableArray alloc] init];
    return _deliveryOptionKeys;
}

-(NSMutableDictionary *)deliveryOptionItems
{
    if (!_deliveryOptionItems)
        _deliveryOptionItems = [[NSMutableDictionary alloc] init];
    return _deliveryOptionItems;
}

-(NSMutableArray *)cancellationPolicyLines
{
    if (!_cancellationPolicyLines)
        _cancellationPolicyLines = [[NSMutableArray alloc] init];
    return _cancellationPolicyLines;
}


@end
