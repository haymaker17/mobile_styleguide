//
//  TaxFormData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//  Description :
//  Object to represent the tax form data node

/*
 <TaxForm>
 <Fields>
 <FormField>
 <Access>RW</Access>
 <CtrlType>edit</CtrlType>
 <Custom>Y</Custom>
 <DataType>VARCHAR</DataType>
 <FtCode>ENTRY_TAX_INFO</FtCode>
 <Id>Custom8</Id>
 <Label>Tax Custom 08</Label>
 <MaxLength>48</MaxLength>
 <Required>Y</Required>
 </FormField>
 ...
 </Fields>
 <TaxAuthKey>100001</TaxAuthKey>
 <TaxFormKey>100001</TaxFormKey>
 </TaxForm>
*/

#import <Foundation/Foundation.h>

@interface TaxFormData : NSObject <NSXMLParserDelegate>

@property (nonatomic, strong) NSMutableDictionary   *formfields;
@property (nonatomic, strong) NSMutableArray	    *fieldkeys;
@property (nonatomic, strong) NSString              *taxAuthKey;
@property (nonatomic, strong) NSString              *taxFormKey;

- (id)initWithName:(NSString *)elementName attributes:(NSDictionary *)attributes parent:(id)parent children:(NSArray *)children parser:(NSXMLParser *)parser;

@end
