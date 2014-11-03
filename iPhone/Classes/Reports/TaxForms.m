//
//  TaxForms.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
//  Class to handle GetTaxFormsV4 mws calls


#import "TaxForms.h"
#import "ExSystem.h"
#import "NSString+Additions.h"


@interface TaxForms ()

// used to build request
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSString					*rpeKey;
@property (nonatomic, strong) NSString					*expKey;
@property (nonatomic, strong) NSString					*lnKey;
@property (nonatomic, strong) NSString					*ctryCode;
@property (nonatomic, strong) NSString					*ctrySubCode;
@property (nonatomic, strong) NSString					*transactionDate;

// list of TaxFormData
@property (nonatomic, strong) TaxFormData               *taxFormData;

// used to delegate parsing back to parent
@property (nonatomic, strong) id                        parent;

@end
    
@implementation TaxForms 


#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder
{
   // [coder encodeObject:self.taxforms forKey:@"taxForms"];
    [coder encodeObject:self.taxFormsData forKey:@"taxFormData"];
    
}

- (id)initWithCoder:(NSCoder *)coder
{
    self.taxFormsData = [coder decodeObjectForKey:@"taxFormData"];
    return self;
}

#pragma mark Instance  Methods

-(NSString *)getMsgIdKey
{
	return GET_TAX_FORMS_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message

	self.rpeKey = parameterBag[@"RPE_KEY"];
    self.expKey = parameterBag[@"EXP_KEY"];
    self.lnKey = parameterBag[@"LN_KEY"];
    // MOB-15084 - handle country and country sub code
    self.ctryCode = parameterBag[@"CTRY_CODE"];
    self.ctrySubCode = parameterBag[@"CTRY_SUB_CODE"];
    self.transactionDate = parameterBag[@"TRANS_DATE"];

	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetTaxFormsV4",
                     [ExSystem sharedInstance].entitySettings.uri];
    
	if (self.rpeKey != nil)
		self.path = [NSString stringWithFormat:@"%@/%@", self.path, self.rpeKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
    
	return msg;
}

/*
 <TaxCriteria>
 <CtryCode>GB</CtryCode>  <! -- Either CtryCode, CtrySubCode or LnKey should be filled -- >
 <CtrySubCode></CtrySubCode>
 <ExpKey>BUSXX</ExpKey>
 <LnKey>11785</LnKey>
 <TransactionDate>2012-12-12</TransactionDate>
 </TaxCriteria>
 */

-(NSString *)makeXMLBody
{
    //knows how to make a post

    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<TaxCriteria>"];

    // MOB-15084
    if ([self.ctryCode lengthIgnoreWhitespace])
        [bodyXML appendString:[NSString stringWithFormat:@"<CtryCode>%@</CtryCode>", [NSString stringByEncodingXmlEntities:self.ctryCode] ]];

    if ([self.ctrySubCode lengthIgnoreWhitespace])
        [bodyXML appendString:[NSString stringWithFormat:@"<CtrySubCode>%@</CtrySubCode>", [NSString stringByEncodingXmlEntities:self.ctrySubCode] ]];

    if ([self.expKey lengthIgnoreWhitespace])
        [bodyXML appendString:[NSString stringWithFormat:@"<ExpKey>%@</ExpKey>", [NSString stringByEncodingXmlEntities:self.expKey] ]];

    if ([self.lnKey lengthIgnoreWhitespace])
        [bodyXML appendString:[NSString stringWithFormat:@"<LnKey>%@</LnKey>", [NSString stringByEncodingXmlEntities:self.lnKey] ]];

    if ([self.transactionDate lengthIgnoreWhitespace])
        [bodyXML appendString:[NSString stringWithFormat:@"<TransactionDate>%@</TransactionDate>", self.transactionDate]];

    
    [bodyXML appendString:@"</TaxCriteria>"];
    
    return bodyXML;
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self parseXMLFileAtData:data];
}


-(id)init
{
    self = [super init];
	if (self)
    {
        self.taxFormsData = [[NSMutableArray alloc] init];
    }
	return self;
}

-(NSString *)getSaveXML
{
    // handle no tax forms
    if (self.taxFormsData.count == 0) {
        return @"";
    }

    // save xml according to documentation
    // http://util-qa.rqa.concur.concurtech.org/qawiki/index.php/Mobile_WS_Expense_Endpoints#AddToReportV4
    NSMutableString *xml = [[NSMutableString alloc] init];
    [xml appendString:@"<TaxForms>"];
    for (int i=0; i<self.taxFormsData.count; i++) {
        TaxFormData *tmp = self.taxFormsData[i];
        if (tmp != nil) {
            [xml appendString:@"<TaxForm>"];

            NSMutableDictionary *fields = tmp.formfields;
            if (fields.count > 0) {
                [xml appendString:@"<Fields>"];
                for (id key in fields) {
                    FormFieldData *field = fields[key];
                    
                    // MOB-15490: set the likey to "N" and value to "NO" if they are empty.
                    if([field.dataType isEqualToString:@"BOOLEANCHAR"] || [field.ctrlType isEqualToString:@"checkbox"])
                    {
                        if( ![field.liKey lengthIgnoreWhitespace] && ![field.fieldValue lengthIgnoreWhitespace])
                        {
                            field.liKey = @"N";
                            field.fieldValue = @"NO";
                        }
                    }
                    [xml appendString:@"<FormField>"];
                    [xml appendString:[NSString stringWithFormat:@"<Id>%@</Id>", [NSString stringByEncodingXmlEntities:field.iD] ]];
                    if ([field.ftCode length])
                        [xml appendString:[NSString stringWithFormat:@"<FtCode>%@</FtCode>", [NSString stringByEncodingXmlEntities:field.ftCode] ]];
                    if ([field.liCode length])
                        [xml appendString:[NSString stringWithFormat:@"<LiCode>%@</LiCode>", [NSString stringByEncodingXmlEntities:field.liCode]]];
                    if ([field.liKey length])
                        [xml appendString:[NSString stringWithFormat:@"<LiKey>%@</LiKey>", [NSString stringByEncodingXmlEntities:field.liKey] ]];
                    if ([field.listKey length])
                        [xml appendString:[NSString stringWithFormat:@"<ListKey>%@</ListKey>", [NSString stringByEncodingXmlEntities:field.listKey] ]];
                    if ([field.parLiKey length])
                        [xml appendString:[NSString stringWithFormat:@"<ParLiKey>%@</ParLiKey>", [NSString stringByEncodingXmlEntities:field.parLiKey] ]];

                    [xml appendString:[NSString stringWithFormat:@"<Value>%@</Value>", field.fieldValue == nil? @"" : [NSString stringByEncodingXmlEntities:field.fieldValue] ]];
                    [xml appendString:@"</FormField>"];
                }
                [xml appendString:@"</Fields>"];
            }

            [xml appendString:[NSString stringWithFormat:@"<TaxAuthKey>%@</TaxAuthKey>", tmp.taxAuthKey]];
            [xml appendString:[NSString stringWithFormat:@"<TaxFormKey>%@</TaxFormKey>", tmp.taxFormKey]];
            [xml appendString:@"</TaxForm>"];
        }
    }
    [xml appendString:@"</TaxForms>"];

    return xml;
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
        NSLog(@"Code %@",[NSString stringWithFormat:@"%i", [parseError code]]);
    }
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	NSLog(@"TaxForms : foundIgnorableWhitespace");
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    // if the currentelement is taxform delegate the taxform class to do the job
    if([elementName isEqualToString:@"TaxForm"])
    {
        self.taxFormData = [[TaxFormData alloc] initWithName:elementName attributes:attributeDict parent:self children:nil parser:parser];
        [self.taxFormsData addObject:self.taxFormData];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    // Done parsing the taxforms so set the delegate back to parent.
    if([elementName isEqualToString:@"TaxForms"])
    {
        [parser setDelegate:self.parent];
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    
}


#pragma mark - utility methods
// Let the other objects call this class 
+ (id)elementWithName:(NSString *)elementName attributes:(NSDictionary *)attributes parent:(id)parent children:(NSArray *)children parser:(NSXMLParser *)parser
{
    return [[[self class] alloc] initWithName:elementName
                                   attributes:attributes parent:parent children:children
                                       parser:parser] ;
}

- (id)initWithName:(NSString *)elementName attributes:(NSDictionary *)attributes parent:(id)parent children:(NSArray *)children parser:(NSXMLParser *)parser
{
    self = [self init];
    if (self)
    {
        [self setParent:parent];
        [parser setDelegate:self]; // SET current object AS DELEGATE
    }
    return self;
}

// TODO : Return all form fields keys from all taxform nodes
// currently returns only the first node formfields

- (NSArray *)getFieldsKeys
{
    NSArray *fkeys = nil;
    
    if([self.taxFormsData count]> 0)
    {
        TaxFormData *taxformData = self.taxFormsData[0];
        fkeys = taxformData.fieldkeys;
    }
    
    return fkeys;
}

// TODO : Return all form fields 
- (NSDictionary *)getFormFields
{
    NSDictionary *formFields = nil;
    
    if([self.taxFormsData count]> 0)
    {
        TaxFormData *taxformData = self.taxFormsData[0];
        formFields = taxformData.formfields;
    }
    
    return formFields;
}
@end
