//
//  CXStatement.h
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

typedef NS_ENUM(NSUInteger, CXParticipant) {
    CXParticipantComputer,
    CXParticipantHuman
};

@interface CXStatement : NSObject

@property (assign) CXParticipant participant;
@property (copy, nonatomic) NSString *text;

- (id)initWithText:(NSString *)text fromParticipant:(CXParticipant)participant;

@end
