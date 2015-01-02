//
//  TaxFormData.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TaxFormData.h"
#import "FormFieldData.h"

@interface TaxFormData ()

@property (nonatomic, strong) NSString			*currentElement;
@property (nonatomic, strong) FormFieldData		*field;
@property BOOL inFormField;
@property (nonatomic, strong) id parent;


@end

@implementation TaxFormData

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder
{
    // [coder encodeObject:self.taxforms forKey:@"taxForms"];
    [coder encodeObject:self.formfields forKey:@"formfields"];
    [coder encodeObject:self.fieldkeys forKey:@"fieldkeys"];
    [coder encodeObject:self.taxAuthKey forKey:@"taxAuthKey"];
    [coder encodeObject:self.taxFormKey forKey:@"taxFormKey"];
    
}

- (id)initWithCoder:(NSCoder *)coder
{
    self.formfields = [coder decodeObjectForKey:@"formfields"];
    self.fieldkeys = [coder decodeObjectForKey:@"fieldkeys"];
    self.taxAuthKey = [coder decodeObjectForKey:@"taxAuthKey"];
    self.taxFormKey = [coder decodeObjectForKey:@"taxFormKey"];
    return self;
}

#pragma mark Instance  Methods

-(id)init
{
    self = [super init];
	if (self)
    {
        self.currentElement = @"";
        self.formfields = [[NSMutableDictionary alloc] init];
        self.fieldkeys = [[NSMutableArray alloc] init];
        [self flushData];
    }
	return self;
}

-(void) flushData
{
    self.currentElement = nil;
	[self.fieldkeys removeAllObjects];
    [self.formfields removeAllObjects];
}

#pragma mark - utility methods

// Some way of storing the form fields, just to be consistent with the ReportEntryData.
-(void)finishField
{
	if (self.field.fieldValue == nil)
		self.field.fieldValue = @"";
	
	if (self.field != nil && self.field.iD != nil && self.field.fieldValue != nil)
	{
		self.formfields[self.field.iD] = self.field;
		[self.fieldkeys addObject:self.field.iD];
        
		self.field = [[FormFieldData alloc] init];
	}
	else if (self.field != nil)
	{
		self.field = [[FormFieldData alloc] init];
	}
}


- (void) updateFormField:(FormFieldData*)ff property:(NSString*)elementName value: (NSString*)propVal
{
	NSString* propName = [FormFieldData getXmlToPropertyMap][elementName];
	if (propName != nil)
	{
        // TODO: refactor using keyvalue encoding
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [ff performSelector:NSSelectorFromString([NSString stringWithFormat:@"set%@:", propName]) withObject:propVal];
#pragma clang diagnostic pop
    }
    else if ([elementName isEqualToString:@"HierKey"])
	{
		ff.hierKey = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"HierLevel"])
	{
		ff.hierLevel = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"ParHierLevel"])
	{
		ff.parHierLevel = [propVal intValue];
	}
	
}


#pragma mark - parser delegates

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
	
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError
{
	if (parseError != nil)
    {
		NSLog(@"Error parsing response for GetTaxFormsV4: Parser Error : %@",[parseError localizedDescription]);
        NSLog(@"Code %@",[NSString stringWithFormat:@"%li", (long)[parseError code]]);
        //				[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters IN RPT FIELD HANDLING currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];

    }
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	NSLog(@"TaxForms : foundIgnorableWhitespace");
    //				[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReportApprovalListData::foundCharacters IN RPT FIELD HANDLING currentElement = %@, string = %@", currentElement, string] Level:MC_LOG_DEBU];

}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    self.currentElement = elementName;
    if ([elementName isEqualToString:@"FormField"])
	{
        self.field = [[FormFieldData alloc] init];
		self.inFormField = YES;
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    if ([elementName isEqualToString:@"FormField"])
	{
		[self finishField];
		self.inFormField = NO;
	}

    self.currentElement = @"";
    if([elementName isEqualToString:@"TaxForm"])
    {
        [parser setDelegate:self.parent];
    }

}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    if(self.inFormField) {
        [self updateFormField:self.field property:self.currentElement value:string];
    } else {
        if([self.currentElement isEqualToString:@"TaxAuthKey"]) {
            self.taxAuthKey = string;
        } else if([self.currentElement isEqualToString:@"TaxFormKey"]) {
            self.taxFormKey = string;
        }
    }
}

#pragma mark -

- (id)initWithName:(NSString *)elementName attributes:(NSDictionary *)attributes parent:(id)parent children:(NSArray *)children parser:(NSXMLParser *)parser
{
    self = [self init];
    if (self)
    {
        self.currentElement = elementName;
        [self setParent:parent];
        [parser setDelegate:self]; // SET current object AS DELEGATE
    }
    return self;
}


@end
