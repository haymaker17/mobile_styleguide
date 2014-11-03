//
//  IgniteChatterPostVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
#import "MobileViewController.h"
#import "ExMsgRespondDelegate.h"
#import "IgniteChatterPostDelegate.h"

@class EntityChatterFeedEntry;

@interface IgniteChatterPostVC : MobileViewController <ExMsgRespondDelegate>
{
    UINavigationBar         *navBar;
    UIImageView             *imgView;
    UITextView              *txtView;
    
    EntityChatterFeedEntry  *feedEntry; // If posting a comment, then this is the the feed entry being commented upon.  For brand new post, use nil.
    
    id<IgniteChatterPostDelegate>  __weak _delegate;
}

@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) IBOutlet UIImageView          *imgView;
@property (nonatomic, strong) IBOutlet UITextView           *txtView;
@property (nonatomic, strong) EntityChatterFeedEntry        *feedEntry;
@property (nonatomic, weak) id<IgniteChatterPostDelegate> delegate;

- (void)sendChatterPost:(NSString*) text;

@end

