//
//  CXStatement.m
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXStatement.h"

@implementation CXStatement

- (id)initWithText:(NSString *)text fromParticipant:(CXParticipant)participant {
    self = [super init];
    
    if (self) {
        self.text = text;
        self.participant = participant;
    }

    return self;
}

@end
