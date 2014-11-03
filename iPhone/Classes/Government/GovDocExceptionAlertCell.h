//
//  GovDocExceptionAlertCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GovDocExceptionAlertCell : UITableViewCell

@property (strong, nonatomic) IBOutlet UIImageView *alertImg;
@property (strong, nonatomic) IBOutlet UILabel *exceptionName;
@property (strong, nonatomic) IBOutlet UILabel *alertName;
@property (strong, nonatomic) IBOutlet UILabel *passOrFail;


@end
