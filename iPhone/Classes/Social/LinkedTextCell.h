//
//  LinkedTextCell.h
//  ConcurMobile
//
//  Created by yiwen on 8/4/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface LinkedTextCell : UITableViewCell 
{
	UITextView		*txtMessage;
}

@property (nonatomic, strong) IBOutlet UITextView *txtMessage;

@end
