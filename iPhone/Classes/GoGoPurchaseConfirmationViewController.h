//
//  GoGoPurchaseConfirmationViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractGoGoViewController.h"
#import "CXCopyableLabel.h"

@interface GoGoPurchaseConfirmationViewController : AbstractGoGoViewController

@property (copy, nonatomic) NSString *confirmationCodeString;
@property (weak, nonatomic) IBOutlet UILabel *confirmationNoticeLabel;
@property (weak, nonatomic) IBOutlet CXCopyableLabel *confirmationCodeLabel;

@end
