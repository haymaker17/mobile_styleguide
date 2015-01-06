//
//  FFFieldProtocol.h
//  ConcurMobile
//
//  Created by Laurent Mery on 15/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol FFFieldProtocol <NSObject>

-(NSArray*)errorsOnValidateField:(FFField*)field;
-(NSString*)iconLabelForField:(FFField*)field;

@end