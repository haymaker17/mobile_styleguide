//
//  CTEFormFields.h
//  ConcurSDK
//
//  Created by laurent mery on 10/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class CTEError;

@interface CTEFormFields: NSObject

@property (copy, nonatomic, readonly) NSString *formID;
@property (strong, nonatomic) CTEError *cteError;

-(CTEFormFields*)formbyID:(NSString*)formID;
-(NSArray*)fieldsOrdered;

@end
