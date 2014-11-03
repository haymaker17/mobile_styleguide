//
//  IgniteChatterPostPrivateVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "ExMsgRespondDelegate.h"
#import "IgniteChatterPostDelegate.h"
#import "IgniteUserPickerDelegate.h"

@class EntityChatterFeedEntry;
@class IgniteUserPickerVC;

@interface IgniteChatterPostPrivateVC : MobileViewController <ExMsgRespondDelegate, UITextViewDelegate, IgniteUserPickerDelegate, UIPopoverControllerDelegate>
{
    UINavigationBar         *navBar;
    UIScrollView            *vwScroll;
    UITextView              *txtRecipients;
    UITextView              *txtComment;
    
    id<IgniteChatterPostDelegate>  __weak _delegate;

    NSMutableArray          *recipientsArray;

    // Popover
    UIPopoverController         *vcPopover;
}

@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) IBOutlet UIScrollView         *vwScroll;
@property (nonatomic, strong) IBOutlet UITextView           *txtRecipients;
@property (nonatomic, strong) IBOutlet UITextView           *txtComment;

@property (nonatomic, weak) id<IgniteChatterPostDelegate> delegate;

@property (nonatomic, strong) NSMutableArray                *recipientsArray;

@property (nonatomic, strong) UIPopoverController           *vcPopover;

@end
