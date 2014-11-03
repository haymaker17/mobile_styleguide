//
//  ReceiptStoreListCell.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RoundedRectView.h"

@interface ReceiptStoreListCell : UITableViewCell {
	RoundedRectView				*imageBackgroundView;
	UIImageView					*thumbImageView;
   	UILabel						*tagLbl;
	UILabel						*imageDateLbl;
	UIActivityIndicatorView		*activityView;
    NSString                    *receiptId;
}

@property (nonatomic,strong) IBOutlet RoundedRectView			*imageBackgroundView;
@property (nonatomic,strong) IBOutlet UIImageView				*thumbImageView;
@property (nonatomic,strong) IBOutlet UILabel					*tagLbl;
@property (nonatomic,strong) IBOutlet UILabel					*imageDateLbl;
@property (nonatomic,strong) IBOutlet UIActivityIndicatorView	*activityView;
@property (nonatomic,strong) NSString                           *receiptId;
@end
