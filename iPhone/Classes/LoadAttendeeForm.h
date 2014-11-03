//
//  LoadAttendeeForm.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"

@class FormFieldData;

@interface LoadAttendeeForm : MsgResponder
{
	NSString				*path;
	NSString				*currentElement;
	NSMutableString			*buildString;
	NSString				*atnTypeKey;
	NSMutableArray			*fields;
	FormFieldData			*currentField;
}

@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSString					*currentElement;
@property (nonatomic, strong) NSMutableString			*buildString;
@property (nonatomic, strong) NSString					*atnTypeKey;
@property (nonatomic, strong) NSMutableArray			*fields;
@property (nonatomic, strong) FormFieldData				*currentField;
@property (nonatomic, strong) NSString					*atnKey;
@property (nonatomic, strong) NSString					*atnTypeCode;

- (void) updateFormField:(FormFieldData*)ff property:(NSString*) elementName value: (NSString*) propVal;
@end
