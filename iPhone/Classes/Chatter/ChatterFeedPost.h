//
//  ChatterPost.h
//  ConcurMobile
//
//  Created by ernest cho on 6/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ChatterFeedTableCell.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterCommentEntry.h"

@interface ChatterFeedPost : NSObject

@property (nonatomic, readwrite, strong) NSString *author;
@property (nonatomic, readwrite, strong) NSString *company;
@property (nonatomic, readwrite, strong) NSString *dateString;
@property (nonatomic, readwrite, strong) NSString *text;

-(id) initWithEntityChatterCommentEntry:(EntityChatterCommentEntry *)chatterCommentEntry withEntityChatterFeedEntry:(EntityChatterFeedEntry *)chatterFeedEntity;
-(id) initWithEntityChatterFeedEntry:(EntityChatterFeedEntry *)chatterFeedEntity;

- (void)getPortraitForImageView:(UIImageView *)view;

@end
