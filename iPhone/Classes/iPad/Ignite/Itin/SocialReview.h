//
//  SocialReview.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SocialReview : NSObject
{
    NSString        *userName, *location, *review, *profileImageName;
    NSString        *date;
    NSInteger       rating;
}

@property (nonatomic, strong) NSString          *userName;
@property (nonatomic, strong) NSString          *location;
@property (nonatomic, strong) NSString          *review;
@property (nonatomic, strong) NSString          *profileImageName;
@property (nonatomic, strong) NSString          *date;
@property NSInteger rating;
@end
