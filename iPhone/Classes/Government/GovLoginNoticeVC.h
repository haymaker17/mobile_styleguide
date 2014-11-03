//
//  GovLoginNoticeVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "LoginHelpTopicVC.h"
#import "EntityWarningMessages.h"

@interface GovLoginNoticeVC : LoginHelpTopicVC<UIScrollViewDelegate>
{
    EntityWarningMessages           *allMessages;
}
@property (nonatomic, strong) EntityWarningMessages         *allMessages;
@end
