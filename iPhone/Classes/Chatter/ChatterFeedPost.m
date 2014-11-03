//
//  ChatterPost.m
//  ConcurMobile
//
//  Created by ernest cho on 6/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterFeedPost.h"
// lets me read from core data.  This is from the Ignite demo.
#import "EntityChatterAuthor.h"
#import "SalesForceUserManager.h"

@interface ChatterFeedPost()
@property (nonatomic, readwrite, strong) NSString *portraitUrl;
@property (nonatomic, readwrite, strong) NSString *identifier;
@end


@implementation ChatterFeedPost

-(id) initWithEntityChatterCommentEntry:(EntityChatterCommentEntry *)chatterCommentEntry withEntityChatterFeedEntry:(EntityChatterFeedEntry *)chatterFeedEntity
{
    self = [super init];
    if (self) {
        self.author = chatterFeedEntity.author.name;
        self.company = chatterFeedEntity.author.companyName;

        self.portraitUrl = chatterFeedEntity.author.smallPhotoUrl;
        self.identifier = chatterFeedEntity.author.identifier;

        NSDateFormatter *dateFormat = [self getRelativeDateFormatter];
        self.dateString = [dateFormat stringFromDate:chatterCommentEntry.createdDate];
        self.text = chatterCommentEntry.text;
    }
    return self;
}

-(id) initWithEntityChatterFeedEntry:(EntityChatterFeedEntry *)chatterFeedEntity
{
    self = [super init];
    if (self) {
        self.author = chatterFeedEntity.author.name;
        self.company = chatterFeedEntity.author.companyName;

        self.portraitUrl = chatterFeedEntity.author.smallPhotoUrl;
        self.identifier = chatterFeedEntity.author.identifier;

        NSDateFormatter *dateFormat = [self getRelativeDateFormatter];
        self.dateString = [dateFormat stringFromDate:chatterFeedEntity.createdDate];
        self.text = chatterFeedEntity.text;
    }
    return self;
}

// Request portrait
- (void)getPortraitForImageView:(UIImageView *)view
{
    NSString *imageCacheName = [NSString stringWithFormat:@"Photo_Small_%@", self.identifier];
    [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:self.portraitUrl RespondToImage:nil IV:view MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
}

// should format dates in relative format. Apple's relative format is different from what Henry specced.
- (NSDateFormatter *)getRelativeDateFormatter
{
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setTimeStyle:NSDateFormatterShortStyle];
    [dateFormat setDateStyle:NSDateFormatterShortStyle];
    [dateFormat setDoesRelativeDateFormatting:YES];
    return dateFormat;
}

@end
