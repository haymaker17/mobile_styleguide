//
//  FieldEditDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 11/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormFieldData.h"

@protocol FieldEditDelegate

-(void) fieldCanceled:(FormFieldData*) field;
-(void) fieldUpdated:(FormFieldData*) field;

@end
