//
//  TaxForms.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//  Description
//  Class to handle the taxforms response from GetTaxFormsV4 MWS
//  Class can also be used to represent the taxforms xml node.

/*
<TaxForms>
    <TaxForm>
        <Fields>
         <FormField>
         ...
         </FormField>
         ...
         </Fields>
          ...  
     </TaxForm>
 </TaxForms>
*/

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "TaxFormData.h"

@interface TaxForms : MsgResponder

// Array to hold the list of tax forms
@property (nonatomic, strong) NSMutableArray	*taxFormsData;

- (id)init;
- (id)initWithName:(NSString *)elementName attributes:(NSDictionary *)attributes
            parent:(id)parent children:(NSArray *)children parser:(NSXMLParser *)parser;


// Return the fieldskeys from all the taxform tags
- (NSArray *)getFieldsKeys;
- (NSDictionary *)getFormFields;

-(NSString *)getSaveXML;

@end
