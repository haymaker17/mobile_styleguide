//
//  GovExpListFieldSearchData.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ListFieldSearchData.h"
#import "FormFieldData.h"
#import "ListItem.h"

@interface GovExpListFieldSearchData : ListFieldSearchData
{
    NSString                *docType;
    NSString                *expDescrip;
    
    FormFieldData           *field;
    ListItem                *li;
}

@property (nonatomic, strong) NSString              *docType;
@property (nonatomic, strong) NSString              *expDescrip;

@property (nonatomic, strong) FormFieldData         *field;
@property (nonatomic, strong) ListItem              *li;

@end
