//
//  IgniteItinShareTripVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "ExMsgRespondDelegate.h"
#import "IgniteItinShareTripDelegate.h"
#import "IgniteUserPickerDelegate.h"

@class EntityChatterFeedEntry;
@class IgniteUserPickerVC;

@interface IgniteItinShareTripVC : MobileViewController <ExMsgRespondDelegate, UITextViewDelegate, IgniteUserPickerDelegate, UIPopoverControllerDelegate>
{
    UINavigationBar         *navBar;
    UIScrollView            *vwScroll;
    UITextView              *txtRecipients;
    UITextView              *txtComment;
    
    id<IgniteItinShareTripDelegate>  __weak _delegate;

    NSString                *itinLocator;
    NSMutableArray          *recipientsArray;

    // Popover
    UIPopoverController         *vcPopover;
}

@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) IBOutlet UIScrollView         *vwScroll;
@property (nonatomic, strong) IBOutlet UITextView           *txtRecipients;
@property (nonatomic, strong) IBOutlet UITextView           *txtComment;

@property (nonatomic, weak) id<IgniteItinShareTripDelegate> delegate;

@property (nonatomic, strong) NSString*                     itinLocator;
@property (nonatomic, strong) NSMutableArray                *recipientsArray;

@property (nonatomic, strong) UIPopoverController           *vcPopover;

@end
