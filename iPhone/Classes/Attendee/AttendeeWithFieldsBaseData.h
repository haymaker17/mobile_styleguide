//
//  AttendeeWithFieldsBaseData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 5/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AttendeeBaseData.h"

@interface AttendeeWithFieldsBaseData : AttendeeBaseData
{
    NSMutableArray			*atnColumns;
    FormFieldData			*currentField;
    BOOL                    inAtnColumns;
}
@property (nonatomic, strong) NSMutableArray			*atnColumns;
@property (nonatomic, strong) FormFieldData             *currentField;
@end
