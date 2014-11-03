//
//  AppsMenuCell.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AppsMenuCell : UITableViewCell {
    UIImageView *ivLogo;
    UILabel *lblAppName;
}

@property (nonatomic, strong) IBOutlet UIImageView *ivLogo;
@property (nonatomic, strong) IBOutlet UILabel *lblAppName;
@end
