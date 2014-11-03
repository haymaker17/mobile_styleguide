//
//  PersonalCard.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//



@interface PersonalCard : NSObject {
}

@property (nonatomic, strong) NSString			*accountNumberLastFour;
@property (nonatomic, strong) NSString			*cardName;
@property (nonatomic, strong) NSString			*crnCode;
@property (nonatomic, strong) NSString			*pcaKey;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

@end
