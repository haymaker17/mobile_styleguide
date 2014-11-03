//
//  ReceiptCameraOverlayView.h
//  TestImagePicker
//
//  Created by Sally Yan on 8/8/13.
//  Copyright (c) 2013 Sally Yan. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol ReceiptCameraOverlayViewDelegate
- (void)didTakePicture:(UIImage *)picture;
- (void)didFinishWithCamera;
- (BOOL)shouldDisplayAlbum;
@end

@interface ReceiptCameraOverlayView : UIView
{
    id <ReceiptCameraOverlayViewDelegate> __weak  delegate;
}

@property (nonatomic, weak) id <ReceiptCameraOverlayViewDelegate> delegate;
@property (weak, nonatomic) IBOutlet UIButton *cancelTapped;
@property (weak, nonatomic) IBOutlet UIButton *flashOnOffTapped;

@property (weak, nonatomic) IBOutlet UIButton *albumTapped;
@property (weak, nonatomic) IBOutlet UIButton *takePictureTapped;
@property (weak, nonatomic) IBOutlet UILabel *lblViewTitle;

-(void)setUIText;

@end
