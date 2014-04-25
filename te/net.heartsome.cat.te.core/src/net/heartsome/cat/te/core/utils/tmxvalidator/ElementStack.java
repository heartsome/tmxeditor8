/**
 * ElementStack.java
 *
 * Version information :
 *
 * Date:2013-12-20
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils.tmxvalidator;

import org.apache.xerces.xni.QName;

/**
 * xni 内部类，纯属copy。
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
class ElementStack {

    //
    // Data
    //

    /** The stack data. */
    protected QName[] fElements;

    /** The size of the stack. */
    protected int fSize;

    //
    // Constructors
    //

    /** Default constructor. */
    public ElementStack() {
        fElements = new QName[10];
        for (int i = 0; i < fElements.length; i++) {
            fElements[i] = new QName();
        }
    } // <init>()

    //
    // Public methods
    //

    /** 
     * Pushes an element on the stack. 
     * <p>
     * <strong>Note:</strong> The QName values are copied into the
     * stack. In other words, the caller does <em>not</em> orphan
     * the element to the stack. Also, the QName object returned
     * is <em>not</em> orphaned to the caller. It should be 
     * considered read-only.
     *
     * @param element The element to push onto the stack.
     *
     * @return Returns the actual QName object that stores the
     */
    public QName pushElement(QName element) {
        if (fSize == fElements.length) {
            QName[] array = new QName[fElements.length * 2];
            System.arraycopy(fElements, 0, array, 0, fSize);
            fElements = array;
            for (int i = fSize; i < fElements.length; i++) {
                fElements[i] = new QName();
            }
        }
        fElements[fSize].setValues(element);
        return fElements[fSize++];
    } // pushElement(QName):QName

    /** 
     * Pops an element off of the stack by setting the values of
     * the specified QName.
     * <p>
     * <strong>Note:</strong> The object returned is <em>not</em>
     * orphaned to the caller. Therefore, the caller should consider
     * the object to be read-only.
     */
    public void popElement(QName element) {
        element.setValues(fElements[--fSize]);
    } // popElement(QName)

    /** Clears the stack without throwing away existing QName objects. */
    public void clear() {
        fSize = 0;
    } // clear()
    
    public void lastElement(QName element) {
    	element.setValues(fElements[fSize - 1]);
    }

} // class ElementStack