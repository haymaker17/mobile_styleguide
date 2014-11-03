//
//  CommentCell.h
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface CommentCell : UITableViewCell {
    UILabel			*lblCommentedBy, *lblComment;
}
@property (nonatomic, strong) IBOutlet UILabel *lblComment;
@property (nonatomic, strong) IBOutlet UILabel *lblCommentedBy;

-(void) resetCellContent:(NSString*)comment commentedBy:(NSString*)commentedBy; 

@end
