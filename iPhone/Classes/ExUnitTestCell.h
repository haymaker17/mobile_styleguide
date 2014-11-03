//
//  ExUnitTestCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ExUnitTestCell : UITableViewCell {
    UILabel                 *lbl;
    UIImageView             *iv;
}
@property (nonatomic, strong) IBOutlet UILabel                 *lbl;
@property (nonatomic, strong) IBOutlet UIImageView             *iv;
@end
