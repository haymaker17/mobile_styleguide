//
//  GovExpenseFormData.h
//  ConcurMobile
//
//  Created by ernest cho on 9/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "FormFieldData.h"
#import "ListItem.h"

@interface GovExpenseFormData : MsgResponderCommon
{
    NSString *docType;
    NSString *expDescrip;
    
    NSMutableArray          *formData;
    FormFieldData           *field;
    ListItem                *li;
    // Response not belong to single field, but the whole GetTMExpenseFormResponse
    NSMutableDictionary     *otherFormAttributes;
    
    BOOL                    inDropDownList;
}

@property (nonatomic, strong) NSString                  *docType;
@property (nonatomic, strong) NSString                  *expDescrip;
@property (nonatomic, strong) NSMutableArray            *formData;
@property (nonatomic, strong) FormFieldData             *field;
@property (nonatomic, strong) ListItem                  *li;
@property (nonatomic, strong) NSMutableDictionary       *otherFormAttributes;

@property BOOL inDropDownList;

-(id) init;
-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;
-(void)fillListItem:(NSString *) string;

@end
