//
//  EditFormHelper.h
//  ConcurMobile
//
//  Created by yiwen on 2/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"

@protocol EditFormDataSource

-(NSMutableArray*)allFields;
//-(NSString*) getCurrencyCodeForField:(FormFieldData*) fld;
-(BOOL) validateAttendees;
-(BOOL) canEdit;
@end

// Store field initialization and validation logic
@interface EditFormHelper : NSObject 
{
	id<EditFormDataSource>	__weak editForm;
}

@property (nonatomic, weak) id<EditFormDataSource>			editForm;

-(id) initWithEditForm:(id<EditFormDataSource>)eform;

-(BOOL) validateFields:(BOOL*)missingReqFlds;
-(void) initFields;
-(BOOL) validateAttendees;

-(BOOL) validateDouble :(NSString*) trimmedVal doubleValue:(double*) dblVal;
-(BOOL) validateInteger:(NSString*) trimmedVal integerValue:(int*)intVal;

-(BOOL) validateField:(FormFieldData*) fld missing:(BOOL*) missingReqFields;

@end
