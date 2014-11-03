//
//  IgniteChatterConversationVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/10/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "IgniteChatterConversationDS.h"
#import "EntityChatterFeedEntry.h"
#import "IgniteChatterConversationDSDelegate.h"
#import "IgniteChatterConversationVCDelegate.h"

@interface IgniteChatterConversationVC : MobileViewController <IgniteChatterConversationDSDelegate>
{
    UITableView                 *tblConversation;
    IgniteChatterConversationDS *dsConversation;

    UINavigationBar             *navBar;
    
    EntityChatterFeedEntry      *feedEntry;

    id<IgniteChatterConversationVCDelegate> __weak _delegate;
}

@property (nonatomic, strong) IBOutlet UITableView          *tblConversation;
@property (nonatomic, strong) IgniteChatterConversationDS   *dsConversation;
@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) EntityChatterFeedEntry        *feedEntry;
@property (nonatomic, weak) id<IgniteChatterConversationVCDelegate> delegate;

@end
